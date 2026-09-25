/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.AttrRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.dropUnlessResumed
import net.mm2d.news.aioi.R
import net.mm2d.news.aioi.util.Launcher
import net.mm2d.news.aioi.util.resolveColor
import org.json.JSONObject
import com.google.android.material.R as MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    popBackStack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Toolbar(
                navigationBehavior = scrollBehavior,
                onBackClicked = dropUnlessResumed { popBackStack() },
            )
        },
    ) { paddingValues ->
        val bottomPadding = paddingValues.calculateBottomPadding()
        val currentBottomPadding by rememberUpdatedState(bottomPadding)
        var webViewGeneration by remember { mutableIntStateOf(0) }
        key(webViewGeneration) {
            AndroidView(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(top = paddingValues.calculateTopPadding())
                    .fillMaxSize(),
                factory = { context ->
                    setUpWebView(
                        context = context,
                        getBottomPadding = { currentBottomPadding },
                        onRenderProcessGone = { webViewGeneration++ },
                    )
                },
                update = { webView ->
                    val client = webView.webViewClient as? LicenseWebViewClient
                    if (client?.isPageFinished == true) {
                        setTheme(webView, bottomPadding)
                    }
                },
                onRelease = { webView ->
                    webView.destroy()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    navigationBehavior: TopAppBarScrollBehavior,
    onBackClicked: () -> Unit,
) {
    TopAppBar(
        scrollBehavior = navigationBehavior,
        title = {
            Text(text = stringResource(id = R.string.menu_license))
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
    )
}

private fun setUpWebView(
    context: Context,
    getBottomPadding: () -> Dp,
    onRenderProcessGone: () -> Unit,
): WebView =
    NestedScrollingWebView(context).also {
        setUp(it, getBottomPadding, onRenderProcessGone)
    }

@SuppressLint("SetJavaScriptEnabled")
private fun setUp(
    webView: WebView,
    getBottomPadding: () -> Dp,
    onRenderProcessGone: () -> Unit,
) {
    webView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    webView.settings.let {
        it.setSupportZoom(false)
        it.displayZoomControls = false
        it.javaScriptEnabled = true
    }
    webView.webViewClient = LicenseWebViewClient(getBottomPadding, onRenderProcessGone)
    webView.loadUrl("file:///android_asset/license.html")
}

private class LicenseWebViewClient(
    private val getBottomPadding: () -> Dp,
    private val onRenderProcessGone: () -> Unit,
) : WebViewClient() {
    var isPageFinished: Boolean = false
        private set

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        if (!request.isForMainFrame) return false
        return Launcher.openCustomTabs(view.context, request.url)
    }

    override fun onPageFinished(
        view: WebView,
        url: String,
    ) {
        isPageFinished = true
        setTheme(view, getBottomPadding())
    }

    override fun onRenderProcessGone(
        view: WebView,
        detail: RenderProcessGoneDetail,
    ): Boolean {
        view.destroy()
        onRenderProcessGone()
        return true
    }
}

private fun setTheme(
    webView: WebView,
    bottomPadding: Dp,
) {
    val context = webView.context
    val theme = JSONObject().also {
        it.put("backgroundPrimary", context.attrToHtmlColor(MR.attr.colorSurfaceContainerLow))
        it.put("backgroundSecondary", context.attrToHtmlColor(MR.attr.colorSurfaceContainerLowest))
        it.put("textPrimary", context.attrToHtmlColor(android.R.attr.textColorPrimary))
        it.put("textSecondary", context.attrToHtmlColor(android.R.attr.textColorSecondary))
        it.put("textLink", context.attrToHtmlColor(android.R.attr.textColorLink))
        it.put("border", context.attrToHtmlColor(MR.attr.colorOutlineVariant))
        it.put("paddingBottom", bottomPadding.value.toInt())
    }.toString()
    webView.evaluateJavascript("setTheme($theme)") {}
}

private fun Context.attrToHtmlColor(
    @AttrRes attr: Int,
): String = "#%06X".format(resolveColor(attr) and 0xFFFFFF)

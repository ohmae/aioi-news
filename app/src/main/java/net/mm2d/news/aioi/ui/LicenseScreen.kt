/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.AttrRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import net.mm2d.news.aioi.R
import net.mm2d.news.aioi.util.Launcher
import net.mm2d.news.aioi.util.resolveColor
import org.json.JSONObject
import com.google.android.material.R as MR

private var bottomPadding: Dp = 0.dp

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
                onBackClicked = { popBackStack() },
            )
        },
    ) { paddingValues ->
        bottomPadding = paddingValues.calculateBottomPadding()
        AndroidView(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize(),
            factory = { setUpWebView(it) },
        )
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
            Image(
                modifier = Modifier
                    .clickable { onBackClicked() }
                    .padding(12.dp),
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            )
        },
    )
}

private fun setUpWebView(context: Context): WebView =
    NestedScrollingWebView(context).also { setUp(it) }

@SuppressLint("SetJavaScriptEnabled")
private fun setUp(webView: WebView) {
    webView.settings.let {
        it.setSupportZoom(false)
        it.displayZoomControls = false
        it.javaScriptEnabled = true
    }
    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest,
        ): Boolean {
            if (!request.isForMainFrame) return false
            return Launcher.openCustomTabs(view.context, request.url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            setTheme(view)
        }
    }
    webView.loadUrl("file:///android_asset/license.html")
}

private fun setTheme(webView: WebView) {
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

private fun Context.attrToHtmlColor(@AttrRes attr: Int): String =
    "#%06X".format(resolveColor(attr) and 0xFFFFFF)

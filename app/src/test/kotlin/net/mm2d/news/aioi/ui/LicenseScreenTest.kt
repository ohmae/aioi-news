/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.core.view.descendants
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import net.mm2d.news.aioi.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
@Suppress("NonAsciiCharacters")
class LicenseScreenTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun `LicenseScreen レンダラーのクラッシュ時にAndroidViewを再構築すること`() {
        verifyRecovery(didCrash = true)
    }

    @Test
    fun `LicenseScreen システムによるレンダラー終了時にAndroidViewを再構築すること`() {
        verifyRecovery(didCrash = false)
    }

    private fun verifyRecovery(
        didCrash: Boolean,
    ) {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        val detail = mockk<RenderProcessGoneDetail> {
            every { didCrash() } returns didCrash
        }
        try {
            activity.setContent {
                AppTheme {
                    LicenseScreen(popBackStack = {})
                }
            }
            // 再生成後のWebViewで再び終了しても復旧できることを確認する。
            repeat(2) {
                lateinit var oldWebView: WebView
                composeRule.runOnIdle {
                    oldWebView = findWebView(activity)
                    val client = oldWebView.webViewClient
                    assertThat(client.onRenderProcessGone(oldWebView, detail)).isTrue()
                    assertThat(oldWebView.parent).isNull()
                    // 終了後の遅延通知や重複通知で旧WebViewを再利用しない。
                    client.onPageFinished(oldWebView, "file:///android_asset/license.html")
                    assertThat(client.onRenderProcessGone(oldWebView, detail)).isTrue()
                }
                composeRule.runOnIdle {
                    val newWebView = findWebView(activity)
                    assertThat(newWebView).isNotSameInstanceAs(oldWebView)
                    assertThat(newWebView.url).isEqualTo("file:///android_asset/license.html")
                    assertThat(newWebView.settings.javaScriptEnabled).isTrue()
                    assertThat(oldWebView.parent).isNull()
                }
            }
        } finally {
            controller.pause().stop().destroy()
        }
    }

    private fun findWebView(
        activity: ComponentActivity,
    ): WebView = (activity.window.decorView as ViewGroup).descendants.filterIsInstance<WebView>().single()
}

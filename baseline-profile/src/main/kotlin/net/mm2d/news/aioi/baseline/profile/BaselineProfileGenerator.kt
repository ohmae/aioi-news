/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.baseline.profile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    @JvmName("generate")
    fun `generate プロファイルを生成する`() {
        val targetAppId = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: InstrumentationRegistry.getArguments().getString("targetPackage")
            ?: "net.mm2d.news.aioi"

        rule.collect(
            packageName = targetAppId,
            includeInStartupProfile = true,
        ) {
            // 1. Cold Startup
            pressHome()
            startActivityAndWait()

            // メイン画面の表示を待機
            device.wait(Until.hasObject(By.text("新着情報")), TIMEOUT_MS)

            // 2. 新着情報一覧（LazyColumn）のスクロール
            device.flingList("whats_new_list", Direction.DOWN)
            device.flingList("whats_new_list", Direction.UP)

            // 3. ボトムナビゲーションで「リンク集」タブに切り替え
            val linksTab = device.findObject(By.text("リンク集"))
            if (linksTab != null) {
                linksTab.click()
                device.waitForIdle()

                // リンク集一覧（LazyColumn）のスクロール
                device.flingList("links_list", Direction.DOWN)
                device.flingList("links_list", Direction.UP)
            }

            // 4. 「新着情報」タブに戻る
            val whatsNewTab = device.findObject(By.text("新着情報"))
            if (whatsNewTab != null) {
                whatsNewTab.click()
                device.waitForIdle()
            }

            // 5. ドロワーを開いてライセンス画面へ遷移
            device.swipe(
                0,
                device.displayHeight / 2,
                device.displayWidth / 2,
                device.displayHeight / 2,
                SWIPE_STEPS,
            )
            device.waitForIdle()

            val licenseItem = device.findObject(By.text("オープンソースライセンス"))
            if (licenseItem != null) {
                licenseItem.click()
                device.waitForIdle()

                // ライセンス画面（WebView）の読み込み待機
                device.wait(Until.hasObject(By.text("オープンソースライセンス")), TIMEOUT_MS)

                // 戻る操作でメイン画面へ復帰
                device.pressBack()
                device.waitForIdle()
            }
        }
    }

    private fun UiDevice.flingList(
        resourceName: String,
        direction: Direction,
    ) {
        // Pagerの遷移やComposeの更新で無効になった要素は、待機時間内で再取得する。
        val completed = wait(
            { device ->
                try {
                    val list = device.findObject(By.res(resourceName))
                    if (list == null) {
                        false
                    } else {
                        list.setGestureMargin(device.displayWidth / 5)
                        list.fling(direction)
                        true
                    }
                } catch (_: StaleObjectException) {
                    false
                }
            },
            TIMEOUT_MS,
        )
        check(completed) { "一覧をスクロールできませんでした: $resourceName ($direction)" }
        waitForIdle()
    }

    companion object {
        private const val TIMEOUT_MS = 5_000L
        private const val SWIPE_STEPS = 20
    }
}

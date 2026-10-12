/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.baseline.profile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmarks {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfiles() = benchmark(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun benchmark(
        compilationMode: CompilationMode,
    ) {
        val targetAppId = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: InstrumentationRegistry.getArguments().getString("targetPackage")
            ?: "net.mm2d.news.aioi"

        rule.measureRepeated(
            packageName = targetAppId,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                device.wait(Until.hasObject(By.text("新着情報")), TIMEOUT_MS)
            },
            measureBlock = {
                val scrollable = device.findObject(By.scrollable(true))
                if (scrollable != null) {
                    scrollable.setGestureMargin(device.displayWidth / 5)
                    scrollable.fling(Direction.DOWN)
                    device.waitForIdle()
                    scrollable.fling(Direction.UP)
                    device.waitForIdle()
                }
            },
        )
    }

    companion object {
        private const val TIMEOUT_MS = 5_000L
    }
}

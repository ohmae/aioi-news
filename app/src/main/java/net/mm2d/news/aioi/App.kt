/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.mm2d.news.aioi.util.CustomTabsHelper

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CustomTabsHelper.initialize(this)
    }
}

/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.res.use

fun Context.isDarkMode(): Boolean =
    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

@ColorInt
fun Context.resolveColor(
    @AttrRes attr: Int,
    @ColorInt defaultColor: Int = Color.BLACK,
): Int = resolveColor(0, attr, defaultColor)

@ColorInt
private fun Context.resolveColor(
    @StyleRes style: Int,
    @AttrRes attr: Int,
    @ColorInt defaultColor: Int,
): Int =
    obtainStyledAttributes(style, intArrayOf(attr)).use {
        it.getColor(0, defaultColor)
    }

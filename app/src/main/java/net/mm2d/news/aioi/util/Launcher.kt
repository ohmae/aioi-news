/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.R as MR

object Launcher {
    private fun openUri(
        context: Context,
        uri: String,
    ): Boolean =
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            context.startActivity(intent)
            true
        }.getOrNull() ?: false

    fun openCustomTabs(
        context: Context,
        uri: String,
    ): Boolean = openCustomTabs(context, Uri.parse(uri))

    fun openCustomTabs(
        context: Context,
        uri: Uri,
    ): Boolean =
        runCatching {
            val scheme =
                if (context.isDarkMode()) {
                    CustomTabsIntent.COLOR_SCHEME_DARK
                } else {
                    CustomTabsIntent.COLOR_SCHEME_LIGHT
                }
            val params = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(context.resolveColor(MR.attr.colorSurfaceContainerLowest))
                .build()
            val intent = CustomTabsIntent.Builder(CustomTabsHelper.session)
                .setShowTitle(true)
                .setColorScheme(scheme)
                .setDefaultColorSchemeParams(params)
                .build()
            intent.intent.setPackage(CustomTabsHelper.packageNameToBind)
            intent.launchUrl(context, uri)
            true
        }.getOrNull() ?: false

    fun openGooglePlay(
        context: Context,
        packageName: String,
    ): Boolean =
        openUri(context, "market://details?id=$packageName") ||
            openCustomTabs(context, "https://play.google.com/store/apps/details?id=$packageName")
}

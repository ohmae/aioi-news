/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.util

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

object CustomTabsHelper : CustomTabsServiceConnection() {
    private val PREFERRED_PACKAGES = listOf(
        // Chrome
        "com.android.chrome",
        // Firefox
        "org.mozilla.firefox",
        // Microsoft Edge
        "com.microsoft.emmx",
    )
    private const val ACTION_CUSTOM_TABS_CONNECTION =
        "android.support.customtabs.action.CustomTabsService"
    var packageNameToBind: String? = null
        private set

    private fun findPackageNameToUse(
        context: Context,
    ): String? {
        packageNameToBind = findPackageNameToUseInner(context)
        return packageNameToBind
    }

    private fun findPackageNameToUseInner(
        context: Context,
    ): String? {
        val browsers = OpenUriUtils.getBrowserPackages(context)
        val candidate = context.packageManager
            .queryIntentServicesCompat(
                Intent(ACTION_CUSTOM_TABS_CONNECTION),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
            .mapNotNull { it.serviceInfo?.packageName }
            .filter { browsers.contains(it) }
        if (candidate.isEmpty()) {
            return null
        }
        if (candidate.size == 1) {
            return candidate[0]
        }
        OpenUriUtils.getDefaultBrowserPackage(context).let {
            if (candidate.contains(it)) {
                return it
            }
        }
        return PREFERRED_PACKAGES.find { candidate.contains(it) } ?: candidate[0]
    }

    private lateinit var application: Application
    private var bound: Boolean = false
    var session: CustomTabsSession? = null
        private set

    fun initialize(
        application: Application,
    ) {
        CustomTabsHelper.application = application
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(CustomTabsBinder())
    }

    private fun bind() {
        if (!bound) {
            val packageName = findPackageNameToUse(application) ?: return
            bound = CustomTabsClient.bindCustomTabsService(application, packageName, this)
        }
    }

    private fun unbind() {
        if (bound) {
            application.unbindService(this)
            bound = false
            session = null
        }
    }

    override fun onCustomTabsServiceConnected(
        name: ComponentName,
        client: CustomTabsClient,
    ) {
        client.warmup(0)
        session = client.newSession(CustomTabsCallback())
    }

    override fun onServiceDisconnected(
        name: ComponentName,
    ) {
        session = null
    }

    class CustomTabsBinder : DefaultLifecycleObserver {
        override fun onStart(
            owner: LifecycleOwner,
        ) {
            bind()
        }

        override fun onStop(
            owner: LifecycleOwner,
        ) {
            unbind()
        }
    }
}

/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi

import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.leakcanary2.FlipperLeakEventListener
import com.facebook.flipper.plugins.leakcanary2.LeakCanary2FlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import leakcanary.LeakCanary
import net.mm2d.news.data.http.OkHttpInterceptorBridge
import okhttp3.logging.HttpLoggingInterceptor

class DebugApp : App() {
    private val entryPoint: DebugAppEntryPoint by lazy {
        EntryPointAccessors.fromApplication(this)
    }
    private val okHttpInterceptorBridge: OkHttpInterceptorBridge by lazy {
        entryPoint.provideOkHttpInterceptorBridge()
    }

    override fun initializeOverrideWhenDebug() {
        setUpStrictMode()
        setUpOkHttp()
        setUpFlipper()
    }

    private fun setUpStrictMode() {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(VmPolicy.Builder().detectDefault().penaltyLog().build())
    }

    private fun VmPolicy.Builder.detectDefault(): VmPolicy.Builder = apply {
        detectActivityLeaks()
        detectLeakedClosableObjects()
        detectLeakedRegistrationObjects()
        detectFileUriExposure()
        detectCleartextNetwork()
        detectContentUriWithoutPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            detectCredentialProtectedWhileLocked()
        }
    }

    private fun setUpOkHttp() {
        okHttpInterceptorBridge.addInterceptor(
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS),
        )
    }

    private fun setUpFlipper() {
        LeakCanary.config = LeakCanary.config.run {
            copy(eventListeners = eventListeners + FlipperLeakEventListener())
        }
        SoLoader.init(this, false)
        if (!FlipperUtils.shouldEnableFlipper(this)) return
        val networkFlipperPlugin = NetworkFlipperPlugin()
        val client = AndroidFlipperClient.getInstance(this)
        client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
        client.addPlugin(NavigationFlipperPlugin.getInstance())
        client.addPlugin(networkFlipperPlugin)
        client.addPlugin(DatabasesFlipperPlugin(this))
        client.addPlugin(SharedPreferencesFlipperPlugin(this))
        client.addPlugin(LeakCanary2FlipperPlugin())
        client.addPlugin(CrashReporterPlugin.getInstance())
        client.start()

        okHttpInterceptorBridge.addNetworkInterceptor(
            FlipperOkhttpInterceptor(networkFlipperPlugin),
        )
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface DebugAppEntryPoint {
        fun provideOkHttpInterceptorBridge(): OkHttpInterceptorBridge
    }
}

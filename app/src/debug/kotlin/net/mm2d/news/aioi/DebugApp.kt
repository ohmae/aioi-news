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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
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
    }

    private fun setUpStrictMode() {
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(VmPolicy.Builder().detectDefault().penaltyLog().build())
    }

    private fun VmPolicy.Builder.detectDefault(): VmPolicy.Builder =
        apply {
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
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY),
        )
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface DebugAppEntryPoint {
        fun provideOkHttpInterceptorBridge(): OkHttpInterceptorBridge
    }
}

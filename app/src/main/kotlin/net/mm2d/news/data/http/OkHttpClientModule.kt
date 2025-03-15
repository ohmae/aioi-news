/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.http

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OkHttpClientModule {
    @Singleton
    @Provides
    fun provideOkHttpClient(
        interceptorBridge: OkHttpInterceptorBridge,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .addNetworkInterceptors(interceptorBridge.networkInterceptors())
            .addInterceptors(interceptorBridge.interceptors())
            .build()

    private fun OkHttpClient.Builder.addNetworkInterceptors(
        interceptors: List<Interceptor>,
    ): OkHttpClient.Builder =
        apply {
            interceptors.forEach { addNetworkInterceptor(it) }
        }

    private fun OkHttpClient.Builder.addInterceptors(
        interceptors: List<Interceptor>,
    ): OkHttpClient.Builder =
        apply {
            interceptors.forEach { addInterceptor(it) }
        }
}

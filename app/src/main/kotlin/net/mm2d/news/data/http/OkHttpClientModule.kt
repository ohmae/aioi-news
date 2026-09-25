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
import dagger.multibindings.Multibinds
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OkHttpClientModule {
    @Multibinds
    fun bindInterceptors(): Set<Interceptor>

    @Multibinds
    fun bindNetworkInterceptors(): Set<Interceptor>

    companion object {
        @Singleton
        @Provides
        fun provideOkHttpClient(
            interceptors: Set<@JvmSuppressWildcards Interceptor>,
            networkInterceptors: Set<@JvmSuppressWildcards Interceptor>,
        ): OkHttpClient =
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .apply {
                    interceptors.forEach { addInterceptor(it) }
                    networkInterceptors.forEach { addNetworkInterceptor(it) }
                }
                .build()
    }
}

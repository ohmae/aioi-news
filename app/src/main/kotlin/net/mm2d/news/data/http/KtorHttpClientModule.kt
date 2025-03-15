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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class KtorHttpClientModule {
    @Singleton
    @Provides
    fun provideKtorHttpClient(
        okHttpClient: OkHttpClient,
    ): HttpClient =
        HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
        }
}

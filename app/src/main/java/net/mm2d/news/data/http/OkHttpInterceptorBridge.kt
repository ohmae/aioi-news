/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.http

import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpInterceptorBridge @Inject constructor() {
    private val networkInterceptors: MutableList<Interceptor> = mutableListOf()
    private val interceptors: MutableList<Interceptor> = mutableListOf()

    fun networkInterceptors(): List<Interceptor> = networkInterceptors
    fun addNetworkInterceptor(interceptor: Interceptor) {
        networkInterceptors.add(interceptor)
    }

    fun interceptors(): List<Interceptor> = interceptors
    fun addInterceptor(interceptor: Interceptor) {
        interceptors.add(interceptor)
    }
}

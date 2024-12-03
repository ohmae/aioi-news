/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.core

import kotlinx.coroutines.flow.StateFlow

interface RssRepository {
    fun getStream(url: String): StateFlow<RssFeed>
    suspend fun visit(url: String, id: String)
    suspend fun fetch(url: String): Result<RssFeed>
}

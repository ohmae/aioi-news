/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.link

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.mm2d.news.core.Link
import net.mm2d.news.core.LinkRepository

class LinkRepositoryImpl(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LinkRepository {
    private var cachedLinks: List<Link>? = null

    override suspend fun getLinks(): List<Link> =
        withContext(dispatcher) {
            cachedLinks ?: readLinks().also { cachedLinks = it }
        }

    private fun readLinks(): List<Link> {
        val text = context.assets.open("links.json")
            .bufferedReader()
            .use { it.readText() }
        return Json.decodeFromString<List<LinkEntity>>(text)
            .map {
                Link(
                    title = it.title,
                    url = it.url,
                )
            }
    }
}

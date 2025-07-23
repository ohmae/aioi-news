/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssRepository
import net.mm2d.news.data.rss.converter.toRssFeed
import net.mm2d.news.data.rss.converter.toRssFeedEntity
import net.mm2d.news.data.rss.converter.toRssItemEntities
import net.mm2d.news.data.rss.converter.toRssItems
import net.mm2d.news.data.rss.database.RssDao
import net.mm2d.news.data.rss.database.RssDatabase
import net.mm2d.news.data.rss.parser.RssParser
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class RssRepositoryImpl(
    private val client: HttpClient,
    database: RssDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RssRepository {
    private val scope: CoroutineScope = CoroutineScope(dispatcher + SupervisorJob())
    private val dao: RssDao = database.dao()

    private val streamMap: MutableMap<String, StateFlow<RssFeed>> = mutableMapOf()

    @OptIn(ExperimentalTime::class)
    override fun getStream(
        url: String,
    ): StateFlow<RssFeed> {
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val feed = dao.getFeed(url)
            if (feed == null || now - feed.fetched > FETCH_INTERVAL) {
                update(url)
            }
        }
        return streamMap.getOrPut(url) { create(url) }
    }

    private fun create(
        url: String,
    ): StateFlow<RssFeed> =
        combineTransform(
            dao.getFeedFlow(url),
            dao.getItems(url),
        ) { feed, items ->
            feed?.let { emit(it.toRssFeed(items.toRssItems())) }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RssFeed.create("", url),
        )

    private suspend fun update(
        url: String,
    ) {
        fetch(url).fold(
            onSuccess = { feed ->
                val currentItems = streamMap[url]?.value?.items ?: emptyList()
                val feedEntity = feed.toRssFeedEntity()
                val itemEntities = feed.toRssItemEntities(currentItems)
                dao.update(feedEntity, itemEntities)
            },
            onFailure = { e ->
                Log.e("RssRepositoryImpl", "fetch failed: $url", e)
            },
        )
    }

    override suspend fun fetch(
        url: String,
    ): Result<RssFeed> =
        withContext(dispatcher) {
            try {
                val data = client.get(url).bodyAsBytes()
                val result = RssParser().parse(url, data) ?: throw IllegalStateException("parse failed")
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun visit(
        url: String,
        id: String,
    ) {
        dao.visit(url, id, true)
    }

    companion object {
        private val FETCH_INTERVAL = 5.minutes.inWholeMilliseconds
    }
}

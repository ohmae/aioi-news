/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.converter

import kotlinx.datetime.Clock.System
import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssItem
import net.mm2d.news.data.database.RssFeedEntity
import net.mm2d.news.data.database.RssItemEntity

fun RssFeed.toRssFeedEntity(): RssFeedEntity =
    RssFeedEntity(
        url = url,
        title = title,
        description = description,
        link = link,
        imageUrl = imageUrl,
        fetched = System.now().toEpochMilliseconds(),
    )

fun RssFeed.toRssItemEntities(currentItems: List<RssItem>): List<RssItemEntity> {
    val currentItemMap = currentItems.associateBy { it.id }
    return items.map { item ->
        val current = currentItemMap[item.id]
        RssItemEntity(
            id = item.id,
            feed = url,
            created = item.created,
            updated = item.updated,
            title = item.title,
            description = item.description,
            content = item.content,
            link = item.link,
            category = item.category,
            imageUrl = item.imageUrl,
            visited = current?.visited ?: false,
        )
    }
}

fun RssFeedEntity.toRssFeed(items: List<RssItem>): RssFeed =
    RssFeed(
        url = url,
        title = title,
        description = description,
        link = link,
        imageUrl = imageUrl,
        items = items,
    )

fun List<RssItemEntity>.toRssItems(): List<RssItem> =
    map {
        RssItem(
            id = it.id,
            created = it.created,
            updated = it.updated,
            title = it.title,
            description = it.description,
            content = it.content,
            link = it.link,
            category = it.category,
            imageUrl = it.imageUrl,
            visited = it.visited,
        )
    }

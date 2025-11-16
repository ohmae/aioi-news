/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssItem
import org.xml.sax.Attributes
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class Rss2Handler(
    url: String,
) : RssHandler {
    private val path: XmlPath = XmlPath()
    private val builder = RssFeedBuilder(url)
    private val itemBuilders: MutableList<RssItemBuilder> = mutableListOf()
    private var workItem: RssItemBuilder? = null

    override fun getFeed(): RssFeed? {
        if (builder.title.isEmpty()) return null
        val items = itemBuilders.mapNotNull { item ->
            if (item.title.isEmpty()) return@mapNotNull null
            if (item.created == 0L) return@mapNotNull null
            RssItem(
                id = item.created.toString() + ":" + item.link,
                created = item.created,
                updated = item.created,
                title = item.title,
                description = item.description,
                content = item.content.toString(),
                link = item.link,
                category = item.category,
                imageUrl = item.imageUrl,
                visited = false,
            )
        }
        if (items.isEmpty()) return null
        return RssFeed(
            url = builder.url,
            title = builder.title,
            description = builder.description,
            link = builder.link,
            imageUrl = builder.imageUrl,
            items = items,
        )
    }

    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes,
    ) {
        path.push(uri, localName)
        if (path.getOrNull(1).matches("", "channel") &&
            path.getOrNull(0).matches("", "item")
        ) {
            workItem = RssItemBuilder()
        }
    }

    override fun endElement(
        uri: String,
        localName: String,
        qName: String,
    ) {
        val tag = path.pop()
        if (path.getOrNull(0).matches("", "channel") &&
            tag.matches("", "item")
        ) {
            workItem?.let {
                itemBuilders.add(it)
            }
            workItem = null
        }
    }

    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int,
    ) {
        val tag = path.getOrNull(0) ?: return
        val text = String(ch, start, length).trim()
        if (text.isEmpty()) return

        if (path.getOrNull(1).matches("", "channel")) {
            when {
                tag.matches("", "title") -> builder.title = text
                tag.matches("", "description") -> builder.description = text
                tag.matches("", "link") -> builder.link = text
            }
            return
        }
        if (path.getOrNull(2).matches("", "channel") &&
            path.getOrNull(1).matches("", "image")
        ) {
            when {
                tag.matches("", "url") -> builder.imageUrl = text

                tag.matches("", "title") -> {
                    if (builder.title.isEmpty()) {
                        builder.title = text
                    }
                }

                tag.matches("", "link") -> {
                    if (builder.link.isEmpty()) {
                        builder.link = text
                    }
                }
            }
            return
        }

        if (path.getOrNull(2).matches("", "channel") &&
            path.getOrNull(1).matches("", "item")
        ) {
            val workItem = workItem ?: return
            when {
                tag.matches("", "title") -> workItem.title = text
                tag.matches("", "description") -> workItem.description = text
                tag.matches("", "link") -> workItem.link = text
                tag.matches("", "pubDate") -> workItem.created = parseDate(text)
                tag.matches("", "category") -> workItem.category = text
                tag.matches("", "image") -> workItem.imageUrl = text
                tag.matches("", "enclosure") -> workItem.imageUrl = text
                tag.matches(NS_CONTENT, "encoded") -> workItem.content.append(text)
            }
        }
    }

    private fun parseDate(
        text: String,
    ): Long =
        try {
            OffsetDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            0L
        }

    companion object {
        private const val NS_CONTENT = "http://purl.org/rss/1.0/modules/content/"
    }
}

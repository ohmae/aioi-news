/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.parser

import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssItem
import org.xml.sax.Attributes
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class Rss1Handler(url: String) : RssHandler {
    private val path: XmlPath = XmlPath()
    private val builder = RssFeedBuilder(url)
    private val itemBuilders: MutableMap<String, RssItemBuilder> = LinkedHashMap()
    private var workItem: RssItemBuilder? = null

    override fun getFeed(): RssFeed? {
        if (builder.title.isEmpty()) return null
        val items = itemBuilders.values.mapNotNull { item ->
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

    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        path.push(uri, localName)
        if (path.getOrNull(3).matches(NS_RSS, "channel") &&
            path.getOrNull(2).matches(NS_RSS, "items") &&
            path.getOrNull(1).matches(NS_RDF, "Seq") &&
            path.getOrNull(0).matches(NS_RDF, "li")
        ) {
            val resource = attributes.getValue(NS_RDF, "resource")
            if (resource.isNullOrEmpty()) return
            itemBuilders[resource] = RssItemBuilder().also {
                it.link = resource
            }
            return
        }
        if (uri == NS_RSS && localName == "item") {
            val about = attributes.getValue(NS_RDF, "about")
            workItem = itemBuilders[about]
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        path.pop()
        if (localName == "item") {
            workItem = null
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        val tag = path.getOrNull(0) ?: return
        val text = String(ch, start, length).trim()
        if (text.isEmpty()) return

        if (path.getOrNull(1).matches(NS_RSS, "channel")) {
            when {
                tag.matches(NS_RSS, "title") -> builder.title = text
                tag.matches(NS_RSS, "description") -> builder.description = text
                tag.matches(NS_RSS, "link") -> builder.link = text
            }
            return
        }
        if (path.getOrNull(1).matches(NS_RSS, "item")) {
            val workItem = workItem ?: return
            when {
                tag.matches(NS_RSS, "title") -> workItem.title = text
                tag.matches(NS_RSS, "description") -> workItem.description = text
                tag.matches(NS_RSS, "link") -> workItem.link = text
                tag.matches(NS_DC, "subject") -> workItem.category = text
                tag.matches(NS_DC, "date") -> workItem.created = parseDate(text)
                tag.matches(NS_CONTENT, "encoded") -> workItem.content.append(text)
            }
        }
    }

    private fun parseDate(text: String): Long =
        try {
            OffsetDateTime.parse(text).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            0L
        }

    companion object {
        private const val NS_RSS = "http://purl.org/rss/1.0/"
        private const val NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        private const val NS_DC = "http://purl.org/dc/elements/1.1/"
        private const val NS_CONTENT = "http://purl.org/rss/1.0/modules/content/"
    }
}

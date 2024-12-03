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

class AtomHandler(url: String) : RssHandler {
    private val path: XmlPath = XmlPath()
    private val builder = RssFeedBuilder(url)
    private val itemBuilders: MutableList<RssItemBuilder> = mutableListOf()
    private var workItem: RssItemBuilder? = null
    private var nameSpaceAtom: String = NS_ATOM

    override fun getFeed(): RssFeed? {
        if (builder.title.isEmpty()) return null
        val items = itemBuilders.mapNotNull { item ->
            if (item.title.isEmpty()) return@mapNotNull null
            val created = if (item.created != 0L) item.created else item.updated
            if (created == 0L) return@mapNotNull null
            val id = item.id.ifEmpty {
                item.created.toString() + ":" + item.link
            }
            RssItem(
                id = id,
                created = created,
                updated = item.updated,
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
        if (localName == "feed" && uri == NS_ATOM_03) {
            nameSpaceAtom = uri
        }
        path.push(uri, localName)
        val tag = path.getOrNull(0) ?: return

        if (path.getOrNull(1).matches(nameSpaceAtom, "feed") &&
            tag.matches(nameSpaceAtom, "link")
        ) {
            if (attributes.getValue("rel") == "alternate") {
                builder.link = attributes.getValue("href")
            }
            return
        }

        if (path.getOrNull(1).matches(nameSpaceAtom, "feed") &&
            tag.matches(nameSpaceAtom, "entry")
        ) {
            workItem = RssItemBuilder()
            return
        }

        val workItem = workItem ?: return
        if (path.getOrNull(1).matches(nameSpaceAtom, "entry") &&
            tag.matches(nameSpaceAtom, "link")
        ) {
            if (attributes.getValue("rel") == "alternate") {
                workItem.link = attributes.getValue("href")
            }
            return
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        val tag = path.pop()
        if (path.getOrNull(0).matches(nameSpaceAtom, "feed") &&
            tag.matches(nameSpaceAtom, "entry")
        ) {
            workItem?.let {
                itemBuilders.add(it)
            }
            workItem = null
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        val tag = path.getOrNull(0) ?: return
        val text = String(ch, start, length).trim()
        if (text.isEmpty()) return

        if (path.getOrNull(1).matches(nameSpaceAtom, "feed")) {
            when {
                tag.matches(nameSpaceAtom, "title") -> builder.title = text
                tag.matches(nameSpaceAtom, "subtitle") -> builder.description = text
                tag.matches(nameSpaceAtom, "tagline") -> builder.description = text
            }
            return
        }
        if (path.getOrNull(2).matches(nameSpaceAtom, "feed") &&
            path.getOrNull(1).matches(nameSpaceAtom, "entry")
        ) {
            val workItem = workItem ?: return
            when {
                tag.matches(nameSpaceAtom, "id") -> workItem.id = text
                tag.matches(nameSpaceAtom, "title") -> workItem.title = text
                tag.matches(nameSpaceAtom, "summary") -> workItem.description = text
                tag.matches(nameSpaceAtom, "content") -> workItem.content.append(text)
                tag.matches(nameSpaceAtom, "category") -> workItem.category = text
                tag.matches(NS_DC, "subject") -> workItem.category = text
                tag.matches(nameSpaceAtom, "published") -> workItem.created = parseDate(text)
                tag.matches(nameSpaceAtom, "issued") -> workItem.created = parseDate(text)
                tag.matches(nameSpaceAtom, "created") -> workItem.created = parseDate(text)
                tag.matches(nameSpaceAtom, "updated") -> workItem.updated = parseDate(text)
                tag.matches(nameSpaceAtom, "modified") -> workItem.updated = parseDate(text)
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
        private const val NS_ATOM_03 = "http://purl.org/atom/ns#"
        private const val NS_ATOM = "http://www.w3.org/2005/Atom"
        private const val NS_DC = "http://purl.org/dc/elements/1.1/"
    }
}

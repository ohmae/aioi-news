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

class Rss2Handler(
    url: String,
) : RssHandler {
    private val path: XmlPath = XmlPath()
    private val builder = RssFeedBuilder(url)
    private val itemBuilders: MutableList<RssItemBuilder> = mutableListOf()
    private var workItem: RssItemBuilder? = null

    private val textBuilder: StringBuilder = StringBuilder()

    override fun getFeed(): RssFeed? {
        if (builder.title.isEmpty()) return null
        val items = itemBuilders.mapNotNull { item ->
            if (item.title.isEmpty()) return@mapNotNull null
            val id = if (item.created != 0L) {
                item.created.toString() + ":" + item.link
            } else {
                item.idWithoutDate()
            }
            RssItem(
                id = id,
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
        textBuilder.setLength(0)
        path.push(uri, localName)
        val tag = path.getOrNull(0) ?: return
        if (path.getOrNull(1).matches("", "channel") &&
            tag.matches("", "item")
        ) {
            workItem = RssItemBuilder()
            return
        }
        val workItem = workItem ?: return
        if (path.getOrNull(2).matches("", "channel") &&
            path.getOrNull(1).matches("", "item") &&
            tag.matches("", "enclosure")
        ) {
            val url = attributes.getValue("url")
            val type = attributes.getValue("type")
            if (!url.isNullOrEmpty() && (type == null || type.startsWith("image/"))) {
                workItem.imageUrl = url
            }
            return
        }
    }

    override fun endElement(
        uri: String,
        localName: String,
        qName: String,
    ) {
        handleTextContent()
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

    private fun handleTextContent() {
        val tag = path.getOrNull(0)
        val text = textBuilder.toString().trim()
        textBuilder.setLength(0)
        if (tag == null || text.isEmpty()) return
        handleText(tag, text)
    }

    private fun handleText(
        tag: XmlTag,
        text: String,
    ) {
        if (path.getOrNull(1).matches("", "channel")) {
            handleChannelText(tag, text)
            return
        }
        if (path.getOrNull(2).matches("", "channel") &&
            path.getOrNull(1).matches("", "image")
        ) {
            handleImageText(tag, text)
            return
        }
        if (path.getOrNull(2).matches("", "channel") &&
            path.getOrNull(1).matches("", "item")
        ) {
            handleItemText(tag, text)
        }
    }

    private fun handleChannelText(
        tag: XmlTag,
        text: String,
    ) {
        when {
            tag.matches("", "title") -> builder.title = text
            tag.matches("", "description") -> builder.description = text
            tag.matches("", "link") -> builder.link = text
        }
    }

    private fun handleImageText(
        tag: XmlTag,
        text: String,
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
    }

    private fun handleItemText(
        tag: XmlTag,
        text: String,
    ) {
        val workItem = workItem ?: return
        when {
            tag.matches("", "title") -> workItem.title = text
            tag.matches("", "description") -> workItem.description = text
            tag.matches("", "link") -> workItem.link = text
            tag.matches("", "guid") -> workItem.id = text
            tag.matches("", "pubDate") -> workItem.created = parseDate(text)
            tag.matches("", "category") -> workItem.category = text
            tag.matches("", "image") -> workItem.imageUrl = text
            tag.matches("", "enclosure") -> workItem.imageUrl = text
            tag.matches(NS_CONTENT, "encoded") -> workItem.content.append(text)
        }
    }

    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int,
    ) {
        textBuilder.appendRange(ch, start, start + length)
    }

    private fun parseDate(
        text: String,
    ): Long = DateParser.parseRfc1123(text)

    companion object {
        private const val NS_CONTENT = "http://purl.org/rss/1.0/modules/content/"
    }
}

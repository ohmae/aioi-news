/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

import net.mm2d.news.core.RssFeed
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

class RssParser {
    private fun createSaxParser(): SAXParser {
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isValidating = false
        return factory.newSAXParser()
    }

    fun parse(url: String, bytes: ByteArray): RssFeed? =
        MediatorHandler(url).also {
            createSaxParser().parse(bytes.inputStream(), it)
        }.getFeed()

    private class MediatorHandler(
        private val url: String,
    ) : DefaultHandler() {
        private var handler: RssHandler? = null

        fun getFeed(): RssFeed? = handler?.getFeed()

        override fun startDocument() {
        }

        override fun endDocument() {
        }

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            if (handler == null) {
                handler = when (localName) {
                    "RDF" -> Rss1Handler(url)
                    "rss" -> Rss2Handler(url)
                    "feed" -> AtomHandler(url)
                    else -> throw IllegalArgumentException("Unknown format: $localName")
                }
            }
            handler?.startElement(uri, localName, qName, attributes)
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            handler?.endElement(uri, localName, qName)
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            handler?.characters(ch, start, length)
        }
    }
}

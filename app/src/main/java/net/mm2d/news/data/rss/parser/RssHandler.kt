/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

import net.mm2d.news.core.RssFeed
import org.xml.sax.Attributes

interface RssHandler {
    fun getFeed(): RssFeed?
    fun startElement(uri: String, localName: String, qName: String, attributes: Attributes)
    fun endElement(uri: String, localName: String, qName: String)
    fun characters(ch: CharArray, start: Int, length: Int)
}

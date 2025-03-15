/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

class XmlPath {
    private val path: ArrayDeque<XmlTag> = ArrayDeque()
    fun push(
        uri: String,
        localName: String,
    ) {
        path.addLast(XmlTag(uri, localName))
    }

    fun pop(): XmlTag? {
        if (path.isEmpty()) return null
        return path.removeLast()
    }

    fun getOrNull(
        index: Int,
    ): XmlTag? = path.getOrNull(path.lastIndex - index)
}

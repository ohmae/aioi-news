/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

data class XmlTag(
    val uri: String,
    val localName: String,
)

fun XmlTag?.matches(uri: String, localName: String): Boolean =
    this != null && this.uri == uri && this.localName == localName

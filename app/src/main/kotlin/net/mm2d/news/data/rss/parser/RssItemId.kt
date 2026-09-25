/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

import java.security.MessageDigest

internal fun RssItemBuilder.idWithoutDate(): String {
    if (id.isNotEmpty()) return "id:$id"
    if (link.isNotEmpty()) return "link:$link"
    val source = listOf(title, description, content.toString()).joinToString("\u0000")
    val digest = MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))
    return "hash:${digest.joinToString("") { "%02x".format(it) }}"
}

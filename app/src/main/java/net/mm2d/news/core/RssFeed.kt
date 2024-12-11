/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.core

data class RssFeed(
    val url: String,
    val title: String,
    val description: String,
    val link: String,
    val imageUrl: String,
    val items: List<RssItem>,
) {
    companion object {
        fun create(
            title: String,
            url: String,
        ) = RssFeed(
            url = url,
            title = title,
            description = "",
            link = "",
            imageUrl = "",
            items = emptyList(),
        )
    }
}

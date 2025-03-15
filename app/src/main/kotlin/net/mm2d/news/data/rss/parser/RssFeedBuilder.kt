/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

class RssFeedBuilder(
    val url: String,
) {
    var title: String = ""
    var description: String = ""
    var link: String = ""
    var imageUrl: String = ""
}

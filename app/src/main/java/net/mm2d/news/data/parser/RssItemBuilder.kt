/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.parser

class RssItemBuilder {
    var id: String = ""
    var created: Long = 0L
    var updated: Long = 0L
    var title: String = ""
    var description: String = ""
    var content: StringBuilder = StringBuilder()
    var link: String = ""
    var category: String = ""
    var imageUrl: String = ""
}

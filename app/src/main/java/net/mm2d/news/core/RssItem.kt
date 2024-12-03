/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.core

data class RssItem(
    val id: String,
    val created: Long,
    val updated: Long,
    val title: String,
    val description: String,
    val content: String,
    val link: String,
    val category: String,
    val imageUrl: String,
    val visited: Boolean,
)

/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.link

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkEntity(
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
)

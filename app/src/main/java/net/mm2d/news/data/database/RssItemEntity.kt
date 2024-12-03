/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "items", primaryKeys = ["id", "feed"])
data class RssItemEntity(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "feed")
    val feed: String,
    @ColumnInfo(name = "created")
    val created: Long,
    @ColumnInfo(name = "updated")
    val updated: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "link")
    val link: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,
    @ColumnInfo(name = "visited")
    val visited: Boolean,
)

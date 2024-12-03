/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RssFeedEntity::class, RssItemEntity::class], version = 1)
abstract class RssDatabase : RoomDatabase() {
    abstract fun dao(): RssDao
}

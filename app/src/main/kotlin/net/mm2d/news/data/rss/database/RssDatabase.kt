/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RssFeedEntity::class, RssItemEntity::class], version = 2)
abstract class RssDatabase : RoomDatabase() {
    abstract fun dao(): RssDao

    companion object {
        val MIGRATION_1_2: Migration =
            object : Migration(1, 2) {
                override fun migrate(
                    db: SupportSQLiteDatabase,
                ) {
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_feed_created` ON `items` (`feed`, `created`)")
                }
            }
    }
}

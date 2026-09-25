/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RssDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        feed: RssFeedEntity,
    )

    @Query("DELETE FROM feeds WHERE url = :url")
    suspend fun deleteFeed(
        url: String,
    )

    @Query("SELECT * FROM feeds WHERE url = :url")
    fun getFeedFlow(
        url: String,
    ): Flow<RssFeedEntity?>

    @Query("SELECT * FROM feeds WHERE url = :url")
    suspend fun getFeed(
        url: String,
    ): RssFeedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        items: List<RssItemEntity>,
    )

    @Query("DELETE FROM items WHERE feed = :feed AND created < :threshold")
    suspend fun deleteItems(
        feed: String,
        threshold: Long,
    )

    @Query("UPDATE items SET visited = :visited WHERE feed = :feed AND id = :id")
    suspend fun visit(
        feed: String,
        id: String,
        visited: Boolean,
    )

    @Query("SELECT * FROM items WHERE feed = :feed ORDER BY created DESC")
    fun getItems(
        feed: String,
    ): Flow<List<RssItemEntity>>

    @Query("SELECT id FROM items WHERE feed = :feed AND visited = 1")
    suspend fun getVisitedItemIds(
        feed: String,
    ): List<String>

    @Transaction
    suspend fun update(
        feed: RssFeedEntity,
        items: List<RssItemEntity>,
    ) {
        val visitedIds = getVisitedItemIds(feed.url).toSet()
        val mergedItems =
            if (visitedIds.isEmpty()) {
                items
            } else {
                items.map { item ->
                    if (visitedIds.contains(item.id)) item.copy(visited = true) else item
                }
            }
        val oldest = mergedItems.minOfOrNull { it.created }
        if (oldest != null) {
            deleteItems(feed.url, oldest)
        }
        insert(feed)
        insert(mergedItems)
    }
}

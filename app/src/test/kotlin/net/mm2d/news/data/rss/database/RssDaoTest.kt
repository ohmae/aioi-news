/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Suppress("NonAsciiCharacters", "RemoveRedundantBackticks")
class RssDaoTest {
    private lateinit var database: RssDatabase
    private lateinit var dao: RssDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RssDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `日付不明の記事は末尾に並び再取得で消えた記事が削除されること`() =
        runTest {
            val feedUrl = "https://example.com/rss"
            val feed = RssFeedEntity(url = feedUrl, title = "テスト")
            fun item(
                id: String,
                created: Long,
            ) = RssItemEntity(
                id = id,
                feed = feedUrl,
                created = created,
                updated = created,
                title = id,
                description = "",
                content = "",
                link = "",
                category = "",
                imageUrl = "",
                visited = false,
            )
            dao.update(feed, listOf(item("old", 500L), item("undated", 0L)))
            dao.update(feed, listOf(item("dated", 1000L), item("undated", 0L)))
            assertThat(dao.getItems(feedUrl).first().map { it.id }).containsExactly("dated", "undated").inOrder()
            dao.visit(feedUrl, "undated", true)

            dao.update(feed, listOf(item("dated", 1000L), item("undated", 0L)))
            assertThat(dao.getItems(feedUrl).first().single { it.id == "undated" }.visited).isTrue()

            dao.update(feed, listOf(item("dated", 1000L)))
            assertThat(dao.getItems(feedUrl).first().map { it.id }).containsExactly("dated")
        }

    @Test
    fun `itemsが空の場合でもクラッシュせず正常にupdateできること`() =
        runTest {
            val feed = RssFeedEntity(url = "https://example.com/rss", title = "テスト")
            dao.update(feed, emptyList())

            val storedFeed = dao.getFeed("https://example.com/rss")
            assertThat(storedFeed).isNotNull()
            assertThat(storedFeed?.title).isEqualTo("テスト")

            val storedItems = dao.getItems("https://example.com/rss").first()
            assertThat(storedItems).isEmpty()
        }

    @Test
    fun `update実行時に既存の既読状態が保持されること`() =
        runTest {
            val feedUrl = "https://example.com/rss"
            val feed = RssFeedEntity(url = feedUrl, title = "テスト")
            val item1 = RssItemEntity(
                id = "item1",
                feed = feedUrl,
                created = 1000L,
                updated = 1000L,
                title = "記事1",
                description = "説明1",
                content = "本文1",
                link = "https://example.com/1",
                category = "カテゴリ",
                imageUrl = "",
                visited = false,
            )

            // 初回挿入
            dao.update(feed, listOf(item1))

            // item1 を既読にする
            dao.visit(feedUrl, "item1", true)

            var storedItems = dao.getItems(feedUrl).first()
            assertThat(storedItems[0].visited).isTrue()

            // 未読フラグ（visited = false）の状態で再取得・更新された場合でも、既読が保持されること
            val updatedItem1 = item1.copy(visited = false)
            val newItem2 = RssItemEntity(
                id = "item2",
                feed = feedUrl,
                created = 2000L,
                updated = 2000L,
                title = "記事2",
                description = "説明2",
                content = "本文2",
                link = "https://example.com/2",
                category = "カテゴリ",
                imageUrl = "",
                visited = false,
            )
            dao.update(feed, listOf(newItem2, updatedItem1))

            storedItems = dao.getItems(feedUrl).first()
            val itemMap = storedItems.associateBy { it.id }
            assertThat(itemMap["item1"]?.visited).isTrue()
            assertThat(itemMap["item2"]?.visited).isFalse()
        }

    @Test
    fun `MIGRATION_1_2 でインデックスが正常に作成されること`() {
        val helper =
            androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory().create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration
                    .builder(ApplicationProvider.getApplicationContext())
                    .name(null)
                    .callback(
                        object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                            override fun onCreate(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                            ) {
                                db.execSQL(
                                    "CREATE TABLE IF NOT EXISTS `items` (" +
                                        "`id` TEXT NOT NULL, " +
                                        "`feed` TEXT NOT NULL, " +
                                        "`created` INTEGER NOT NULL, " +
                                        "`updated` INTEGER NOT NULL, " +
                                        "`title` TEXT NOT NULL, " +
                                        "`description` TEXT NOT NULL, " +
                                        "`content` TEXT NOT NULL, " +
                                        "`link` TEXT NOT NULL, " +
                                        "`category` TEXT NOT NULL, " +
                                        "`imageUrl` TEXT NOT NULL, " +
                                        "`visited` INTEGER NOT NULL, " +
                                        "PRIMARY KEY(`id`, `feed`))",
                                )
                            }

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) {
                            }
                        },
                    ).build(),
            )
        val db = helper.writableDatabase
        RssDatabase.MIGRATION_1_2.migrate(db)

        val cursor = db.query("PRAGMA index_list('items')")
        var hasIndex = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == "index_items_feed_created") {
                hasIndex = true
            }
        }
        cursor.close()
        db.close()
        assertThat(hasIndex).isTrue()
    }
}

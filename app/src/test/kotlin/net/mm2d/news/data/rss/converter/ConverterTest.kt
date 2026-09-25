/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.converter

import com.google.common.truth.Truth.assertThat
import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssItem
import org.junit.Test

@Suppress("NonAsciiCharacters")
class ConverterTest {
    @Test
    fun `toRssItemEntities で RssItem が RssItemEntity に正しく変換されること`() {
        val item = RssItem(
            id = "1",
            created = 1000L,
            updated = 2000L,
            title = "タイトル",
            description = "説明",
            content = "本文",
            link = "https://example.com/1",
            category = "カテゴリ",
            imageUrl = "https://example.com/1.png",
            visited = false,
        )
        val feed = RssFeed(
            url = "https://example.com/rss",
            title = "フィード",
            description = "フィード説明",
            link = "https://example.com",
            imageUrl = "https://example.com/image.png",
            items = listOf(item),
        )

        val entities = feed.toRssItemEntities()
        assertThat(entities).hasSize(1)
        with(entities[0]) {
            assertThat(id).isEqualTo("1")
            assertThat(this.feed).isEqualTo("https://example.com/rss")
            assertThat(created).isEqualTo(1000L)
            assertThat(updated).isEqualTo(2000L)
            assertThat(title).isEqualTo("タイトル")
            assertThat(description).isEqualTo("説明")
            assertThat(content).isEqualTo("本文")
            assertThat(link).isEqualTo("https://example.com/1")
            assertThat(category).isEqualTo("カテゴリ")
            assertThat(imageUrl).isEqualTo("https://example.com/1.png")
            assertThat(visited).isFalse()
        }
    }
}

/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

@Suppress("NonAsciiCharacters", "RemoveRedundantBackticks")
class RssParserTest {
    @Test
    fun `RSS2で日付がない記事のIDが安定して重複しないこと`() {
        val xml = """
            <rss version="2.0"><channel><title>フィード</title>
                <item><title>記事1</title><guid>article-1</guid></item>
                <item><title>記事2</title><description>説明2</description></item>
                <item><title>記事3</title><description>説明3</description></item>
            </channel></rss>
        """.trimIndent()
        val parser = RssParser()
        val first = parser.parse("https://example.com/rss", xml.toByteArray())!!
        val second = parser.parse("https://example.com/rss", xml.toByteArray())!!

        assertThat(first.items.map { it.created }).containsExactly(0L, 0L, 0L)
        assertThat(first.items.map { it.id }.toSet()).hasSize(3)
        assertThat(first.items.map { it.id }).containsExactlyElementsIn(second.items.map { it.id }).inOrder()
    }

    @Test
    fun `RSS1で日付がない記事にrdf aboutを使うこと`() {
        val xml = """
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns="http://purl.org/rss/1.0/">
                <channel rdf:about="https://example.com/rss">
                    <title>フィード</title>
                    <items><rdf:Seq><rdf:li rdf:resource="https://example.com/1" /></rdf:Seq></items>
                </channel>
                <item rdf:about="https://example.com/1"><title>記事1</title></item>
            </rdf:RDF>
        """.trimIndent()

        val item = RssParser().parse("https://example.com/rss", xml.toByteArray())!!.items.single()

        assertThat(item.created).isEqualTo(0L)
        assertThat(item.id).isEqualTo("id:https://example.com/1")
    }

    @Test
    fun `Atomで日付がない記事のIDがリンクから作られること`() {
        val xml = """
            <feed xmlns="http://www.w3.org/2005/Atom">
                <title>フィード</title>
                <entry><title>記事1</title><link href="https://example.com/1" /></entry>
            </feed>
        """.trimIndent()

        val item = RssParser().parse("https://example.com/rss", xml.toByteArray())!!.items.single()

        assertThat(item.created).isEqualTo(0L)
        assertThat(item.id).isEqualTo("link:https://example.com/1")
    }

    @Test
    fun `RSS2で実体参照を含むタイトルや説明文が欠落せずパースされること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <rss version="2.0">
                <channel>
                    <title>相生市 &amp; 播磨</title>
                    <link>https://example.com</link>
                    <description>相生市の新着情報 &lt;最新&gt;</description>
                    <item>
                        <title>相生市 &amp; 新着情報</title>
                        <link>https://example.com/1</link>
                        <description>本文です &amp; 詳細情報</description>
                        <pubDate>Mon, 06 Sep 2021 16:45:00 +0900</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val parser = RssParser()
        val feed = parser.parse("https://example.com/rss", xml.toByteArray())

        assertThat(feed).isNotNull()
        assertThat(feed!!.title).isEqualTo("相生市 & 播磨")
        assertThat(feed.description).isEqualTo("相生市の新着情報 <最新>")
        assertThat(feed.items).hasSize(1)
        assertThat(feed.items[0].title).isEqualTo("相生市 & 新着情報")
        assertThat(feed.items[0].description).isEqualTo("本文です & 詳細情報")
    }

    @Test
    fun `RSS1で実体参照を含むタイトルが欠落せずパースされること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns="http://purl.org/rss/1.0/"
                     xmlns:dc="http://purl.org/dc/elements/1.1/">
                <channel rdf:about="https://example.com/rss">
                    <title>チャンネル &amp; タイトル</title>
                    <link>https://example.com</link>
                    <description>説明</description>
                    <items>
                        <rdf:Seq>
                            <rdf:li rdf:resource="https://example.com/1" />
                        </rdf:Seq>
                    </items>
                </channel>
                <item rdf:about="https://example.com/1">
                    <title>記事 &amp; タイトル</title>
                    <link>https://example.com/1</link>
                    <description>記事説明</description>
                    <dc:date>2021-09-06T16:45:00+09:00</dc:date>
                </item>
            </rdf:RDF>
        """.trimIndent()

        val parser = RssParser()
        val feed = parser.parse("https://example.com/rss", xml.toByteArray())

        assertThat(feed).isNotNull()
        assertThat(feed!!.title).isEqualTo("チャンネル & タイトル")
        assertThat(feed.items).hasSize(1)
        assertThat(feed.items[0].title).isEqualTo("記事 & タイトル")
    }

    @Test
    fun `Atomで実体参照を含むタイトルが欠落せずパースされること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
                <title>フィード &amp; タイトル</title>
                <link href="https://example.com" />
                <entry>
                    <id>tag:example.com,2021:1</id>
                    <title>記事 &amp; タイトル</title>
                    <link href="https://example.com/1" />
                    <updated>2021-09-06T16:45:00+09:00</updated>
                </entry>
            </feed>
        """.trimIndent()

        val parser = RssParser()
        val feed = parser.parse("https://example.com/rss", xml.toByteArray())

        assertThat(feed).isNotNull()
        assertThat(feed!!.title).isEqualTo("フィード & タイトル")
        assertThat(feed.items).hasSize(1)
        assertThat(feed.items[0].title).isEqualTo("記事 & タイトル")
    }

    @Test
    fun `RSS2でenclosure属性からimageUrlがパースされること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <rss version="2.0">
                <channel>
                    <title>タイトル</title>
                    <link>https://example.com</link>
                    <item>
                        <title>記事タイトル</title>
                        <link>https://example.com/1</link>
                        <enclosure url="https://example.com/image.jpg" length="12345" type="image/jpeg" />
                        <pubDate>Mon, 06 Sep 2021 16:45:00 +0900</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val parser = RssParser()
        val feed = parser.parse("https://example.com/rss", xml.toByteArray())

        assertThat(feed).isNotNull()
        assertThat(feed!!.items[0].imageUrl).isEqualTo("https://example.com/image.jpg")
    }

    @Test
    fun `Atomでcategory属性termとlinkのenclosure画像がパースされること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
                <title>フィード</title>
                <link href="https://example.com" />
                <entry>
                    <id>tag:example.com,2021:1</id>
                    <title>記事タイトル</title>
                    <link href="https://example.com/1" />
                    <link rel="enclosure" type="image/png" href="https://example.com/thumbnail.png" />
                    <category term="イベント" label="イベント情報" />
                    <updated>2021-09-06T16:45:00+09:00</updated>
                </entry>
            </feed>
        """.trimIndent()

        val parser = RssParser()
        val feed = parser.parse("https://example.com/rss", xml.toByteArray())

        assertThat(feed).isNotNull()
        assertThat(feed!!.items[0].category).isEqualTo("イベント情報")
        assertThat(feed.items[0].imageUrl).isEqualTo("https://example.com/thumbnail.png")
    }

    @Test
    fun `XXEの外部実体参照が含まれていても安全に処理されること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <!DOCTYPE rss [
                <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <rss version="2.0">
                <channel>
                    <title>&xxe;</title>
                    <link>https://example.com</link>
                    <item>
                        <title>記事</title>
                        <link>https://example.com/1</link>
                        <pubDate>Mon, 06 Sep 2021 16:45:00 +0900</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val parser = RssParser()
        val feed = runCatching { parser.parse("https://example.com/rss", xml.toByteArray()) }.getOrNull()

        // DOCTYPE禁止機能によりパース例外となるか、あるいは実体参照が展開されずに空またはそのまま扱われること
        if (feed != null) {
            assertThat(feed.title).doesNotContain("root:")
        }
    }

    @Test
    fun `RSS2でguidが存在する場合は記事IDとして採用されること`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <rss version="2.0">
                <channel>
                    <title>タイトル</title>
                    <link>https://example.com</link>
                    <item>
                        <title>記事1</title>
                        <link>https://example.com/1</link>
                        <guid>custom-guid-12345</guid>
                        <pubDate>Mon, 06 Sep 2021 16:45:00 +0900</pubDate>
                    </item>
                    <item>
                        <title>記事2</title>
                        <link>https://example.com/2</link>
                        <pubDate>Mon, 06 Sep 2021 16:45:00 +0900</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val parser = RssParser()
        val feed = parser.parse("https://example.com/rss", xml.toByteArray())

        assertThat(feed).isNotNull()
        assertThat(feed!!.items).hasSize(2)
        assertThat(feed.items[0].id).isEqualTo("custom-guid-12345")
        assertThat(feed.items[1].id).isEqualTo("${feed.items[1].created}:https://example.com/2")
    }
}

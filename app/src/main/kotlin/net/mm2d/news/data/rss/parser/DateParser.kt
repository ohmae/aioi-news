/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.parser

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal object DateParser {
    fun parseRfc1123(
        text: String,
    ): Long {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return 0L
        return runCatching {
            ZonedDateTime.parse(trimmedText, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
        }.recoverCatching {
            parseIso8601(trimmedText)
        }.getOrDefault(0L)
    }

    fun parseIso8601(
        text: String,
    ): Long {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return 0L
        return runCatching {
            OffsetDateTime.parse(trimmedText).toInstant().toEpochMilli()
        }.recoverCatching {
            Instant.parse(trimmedText).toEpochMilli()
        }.getOrDefault(0L)
    }
}

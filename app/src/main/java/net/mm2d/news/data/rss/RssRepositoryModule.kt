/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import net.mm2d.news.core.RssRepository
import net.mm2d.news.data.rss.database.RssDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RssRepositoryModule {
    @Singleton
    @Provides
    fun provideRssRepository(
        client: HttpClient,
        database: RssDatabase,
    ): RssRepository = RssRepositoryImpl(
        client = client,
        database = database,
    )
}

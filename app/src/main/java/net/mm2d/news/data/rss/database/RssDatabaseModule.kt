/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.rss.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RssDatabaseModule {
    @Singleton
    @Provides
    fun provideRssDatabase(
        @ApplicationContext
        context: Context,
    ): RssDatabase =
        Room.databaseBuilder(
            context,
            RssDatabase::class.java,
            DB_NAME,
        ).build()

    companion object {
        const val DB_NAME = "rss.db"
    }
}

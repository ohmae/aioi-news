/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.data.link

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.mm2d.news.core.LinkRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LinkRepositoryModule {
    @Singleton
    @Provides
    fun provideLinkRepository(
        @ApplicationContext
        context: Context,
    ): LinkRepository =
        LinkRepositoryImpl(
            context = context,
        )
}

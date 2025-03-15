/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssRepository
import javax.inject.Inject

@HiltViewModel
class WhatsNewViewModel @Inject constructor(
    private val rssRepository: RssRepository,
) : ViewModel() {
    fun feedStream(): StateFlow<RssFeed> = rssRepository.getStream(URL)

    fun visit(
        id: String,
    ) {
        viewModelScope.launch {
            rssRepository.visit(URL, id)
        }
    }

    companion object {
        private const val URL = "https://www.city.aioi.lg.jp/rss/10/list1.xml"
    }
}

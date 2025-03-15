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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.mm2d.news.core.Link
import net.mm2d.news.core.LinkRepository
import javax.inject.Inject

@HiltViewModel
class LinkViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
) : ViewModel() {
    private val linksFlow: MutableStateFlow<List<Link>> = MutableStateFlow(emptyList())

    private var initialized: Boolean = false

    fun initialize() {
        if (initialized) return
        initialized = true

        viewModelScope.launch {
            linksFlow.value = linkRepository.getLinks()
        }
    }

    fun getLinksStream(): StateFlow<List<Link>> = linksFlow
}

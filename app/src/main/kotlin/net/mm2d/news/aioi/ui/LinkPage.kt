/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.mm2d.news.aioi.R
import net.mm2d.news.aioi.util.Launcher
import net.mm2d.news.core.Link

@Composable
fun LinkPage(
    modifier: Modifier = Modifier,
    viewModel: LinkViewModel = hiltViewModel(),
) {
    viewModel.initialize()
    val links by viewModel.getLinksStream().collectAsState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(links) { index, link ->
            Item(link)
            if (index != links.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun LazyItemScope.Item(
    link: Link,
    modifier: Modifier = Modifier,
) {
    val transitionState = remember {
        MutableTransitionState(false).also {
            it.targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = fadeIn(),
        modifier = Modifier
            .animateItem(
                placementSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold,
                ),
            ),
    ) {
        val context = LocalContext.current
        Row(
            modifier = modifier
                .clickable { Launcher.openCustomTabs(context, link.url) }
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = modifier
                    .weight(1f),
            ) {
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = link.url,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
            )
        }
    }
}

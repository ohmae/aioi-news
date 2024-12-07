/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.datetime.Clock
import net.mm2d.news.aioi.R
import net.mm2d.news.aioi.util.Launcher
import net.mm2d.news.aioi.util.doOnStop
import net.mm2d.news.core.RssFeed
import net.mm2d.news.core.RssItem
import kotlin.time.Duration.Companion.days

@Composable
fun WhatsNewPage(
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val feed: RssFeed by viewModel.feedStream().collectAsState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(feed.items) { index, item ->
            Item(item = item)
            if (index != feed.items.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

private val newInterval = 7.days.inWholeMilliseconds

@Composable
private fun LazyItemScope.Item(
    item: RssItem,
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
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
        val lifecycleOwner = LocalLifecycleOwner.current

        ItemContent(
            item = item,
            modifier = modifier
                .clickable {
                    Launcher.openCustomTabs(context, item.link)
                    lifecycleOwner.doOnStop {
                        viewModel.visit(item.id)
                    }
                }
                .background(
                    color = if (item.visited) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        MaterialTheme.colorScheme.background
                    },
                ),
        )
    }
}

@Composable
private fun ItemContent(
    item: RssItem,
    modifier: Modifier = Modifier,
) {
    val now = Clock.System.now().toEpochMilliseconds()
    val isNew = !item.visited && now - item.created < newInterval
    Row(
        modifier = modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            val textColor = if (item.visited) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isNew) {
                    Text(
                        text = "NEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = with(LocalDensity.current) {
                                8.dp.toSp()
                            },
                        ),
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = MaterialTheme.colorScheme.error)
                            .padding(horizontal = 6.dp, vertical = 0.dp),
                    )
                }
                Text(
                    text = item.title,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (item.category.isNotEmpty()) {
                    Text(
                        text = item.category,
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text(
                    text = DateFormat.format("MM/dd HH:mm", item.created).toString(),
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
        )
    }
}

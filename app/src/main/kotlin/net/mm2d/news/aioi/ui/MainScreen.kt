/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mm2d.news.aioi.R

@Composable
fun MainScreen(
    navigateToLicense: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    BackHandler(
        enabled = drawerState.isOpen,
        onBack = { scope.launch { drawerState.close() } },
    )
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = drawerState,
                navigateToLicense = navigateToLicense,
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            )
        },
    ) {
        val pagerState = rememberPagerState(pageCount = { 2 })
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { TopBar(drawerState = drawerState) },
            bottomBar = {
                BottomNavigationBar(
                    pagerState = pagerState,
                    onSelected = {
                        scope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    },
                )
            },
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding),
            ) {
                when (it) {
                    0 -> WhatsNewPage()
                    1 -> LinkPage()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    drawerState: DrawerState,
) {
    val scope = rememberCoroutineScope()
    Surface(shadowElevation = 2.dp) {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    maxLines = 1,
                )
            },
        )
    }
}

@Composable
private fun BottomNavigationBar(
    pagerState: PagerState,
    onSelected: (Int) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = pagerState.currentPage == 0,
            onClick = { onSelected(0) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                )
            },
            label = {
                Text(text = stringResource(id = R.string.title_whats_new))
            },
        )
        NavigationBarItem(
            selected = pagerState.currentPage == 1,
            onClick = { onSelected(1) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                )
            },
            label = {
                Text(text = stringResource(id = R.string.title_links))
            },
        )
    }
}

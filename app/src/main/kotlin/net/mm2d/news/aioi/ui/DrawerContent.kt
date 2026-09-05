/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mm2d.news.aioi.BuildConfig
import net.mm2d.news.aioi.Constants
import net.mm2d.news.aioi.R
import net.mm2d.news.aioi.util.Launcher

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    navigateToLicense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val items = remember {
        listOf(
            Item(
                textId = R.string.menu_license,
                onClick = {
                    navigateToLicense()
                    scope.launch { drawerState.close() }
                },
            ),
            Item(
                textId = R.string.menu_privacy_policy,
                onClick = {
                    Launcher.openCustomTabs(context, Constants.PRIVACY_POLICY_URL)
                    scope.launch { drawerState.close() }
                },
            ),
            Item(
                textId = R.string.menu_play_store,
                onClick = {
                    Launcher.openGooglePlay(context, Constants.PACKAGE_NAME)
                    scope.launch { drawerState.close() }
                },
            ),
            Item(
                textId = R.string.menu_github,
                onClick = {
                    Launcher.openCustomTabs(context, Constants.GITHUB_URL)
                    scope.launch { drawerState.close() }
                },
            ),
        )
    }

    ModalDrawerSheet(
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn {
            items(items) { item ->
                Surface(
                    onClick = item.onClick,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(id = item.textId),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            item {
                Surface(
                    onClick = {},
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(id = R.string.menu_version),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Item(
    val textId: Int,
    val onClick: () -> Unit,
)

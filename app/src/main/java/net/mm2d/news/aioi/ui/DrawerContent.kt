/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
    ModalDrawerSheet(
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn {
            item {
                Text(
                    text = stringResource(id = R.string.menu_license),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable {
                            navigateToLicense()
                            scope.launch { drawerState.close() }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                )
                HorizontalDivider()
            }
            item {
                Text(
                    text = stringResource(id = R.string.menu_privacy_policy),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable {
                            Launcher.openCustomTabs(context, Constants.PRIVACY_POLICY_URL)
                            scope.launch { drawerState.close() }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                )
                HorizontalDivider()
            }
            item {
                Text(
                    text = stringResource(id = R.string.menu_play_store),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable {
                            Launcher.openGooglePlay(context, Constants.PACKAGE_NAME)
                            scope.launch { drawerState.close() }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                )
                HorizontalDivider()
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_version),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

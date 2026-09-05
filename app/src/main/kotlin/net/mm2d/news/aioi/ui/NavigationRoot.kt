/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.Serializable

@Serializable
private sealed interface MainNavKey : NavKey {
    @Serializable
    object Main : MainNavKey

    @Serializable
    object License : MainNavKey
}

private val navGraph: NavGraph<MainNavKey> = navGraph {
    MainNavKey.Main::class leadsTo MainNavKey.License::class
}

@Composable
fun NavigationRoot() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navigator = rememberNavigator(MainNavKey.Main, navGraph) {
        onBackPressedDispatcher?.onBackPressed()
    }
    val entryProvider = remember(navigator) { mainEntryProvider(navigator) }
    NavigationDisplay(
        navigator = navigator,
        entryProvider = entryProvider,
    )
}

private fun mainEntryProvider(
    navigator: Navigator<MainNavKey>,
): (MainNavKey) -> NavEntry<MainNavKey> =
    entryProvider {
        navigator.entry<MainNavKey.Main> {
            MainScreen(
                navigateToLicense = {
                    navigator.navigate(MainNavKey.License)
                },
            )
        }
        navigator.entry<MainNavKey.License> { navKey ->
            LicenseScreen(
                popBackStack = {
                    navigator.goBack(navKey)
                },
            )
        }
    }

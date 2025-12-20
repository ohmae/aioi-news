/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import net.mm2d.news.aioi.ui.theme.NavigationSpec

private sealed interface MainNavKey : NavKey {
    @Serializable
    object Main : MainNavKey

    @Serializable
    object License : MainNavKey
}

private val navGraph: NavGraph = navGraph {
    MainNavKey.Main leadsTo MainNavKey.License
}

@Composable
fun NavigationRoot() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navigator = rememberNavigator(MainNavKey.Main, navGraph) {
        onBackPressedDispatcher?.onBackPressed()
    }
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = NavigationSpec.transition(),
        popTransitionSpec = NavigationSpec.popTransition(),
        predictivePopTransitionSpec = NavigationSpec.predictivePopTransition(),
        entryProvider = mainEntryProvider(navigator),
    )
}

private fun mainEntryProvider(
    navigator: Navigator,
): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        entry<MainNavKey.Main> {
            MainScreen(
                navigateToLicense = {
                    navigator.navigate(MainNavKey.License)
                },
            )
        }
        entry<MainNavKey.License> {
            LicenseScreen(
                popBackStack = {
                    navigator.goBack()
                },
            )
        }
    }

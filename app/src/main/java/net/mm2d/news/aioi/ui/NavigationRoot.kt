/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object MainScreen

@Serializable
object LicenseScreen

@Composable
fun NavigationRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainScreen,
        enterTransition = {
            fadeIn() + slideIn(initialOffset = { IntOffset(0, 0) })
        },
        exitTransition = {
            fadeOut() + slideOut(targetOffset = { IntOffset(-it.width, 0) })
        },
        popEnterTransition = {
            fadeIn() + slideIn(initialOffset = { IntOffset(0, 0) })
        },
        popExitTransition = {
            fadeOut() + slideOut(targetOffset = { IntOffset(it.width, 0) })
        },
    ) {
        composable<MainScreen> {
            MainScreen(
                navigateToLicense = {
                    navController.navigate(LicenseScreen)
                },
            )
        }
        composable<LicenseScreen> {
            LicenseScreen(
                popBackStack = {
                    navController.popBackStack()
                },
            )
        }
    }
}

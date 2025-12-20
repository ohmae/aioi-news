/*
 * Copyright (c) 2025 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import kotlin.reflect.KClass

@Composable
fun rememberNavigator(
    initialKey: NavKey,
    navGraph: NavGraph,
    onExit: () -> Unit,
): Navigator {
    val backStack = rememberNavBackStack(initialKey)
    return remember {
        Navigator(backStack, navGraph, onExit)
    }
}

class Navigator(
    val backStack: NavBackStack<NavKey>,
    private val navGraph: NavGraph,
    private val onExit: () -> Unit,
) {
    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else {
            onExit()
        }
    }

    fun navigate(
        to: NavKey,
    ) {
        if (backStack.isEmpty()) return
        val from = backStack.last()
        if (from == to) {
            Log.v("NavGraph", "from: $from, to: $to is same.")
            return // 連打無効
        }
        if (!navGraph.canNavigate(from, to)) {
            Log.e("NavGraph", "from: $from, to: $to is not allowed.")
            return
        }
        backStack.add(to)
    }
}

interface NavGraph {
    fun canNavigate(
        from: NavKey,
        to: NavKey,
    ): Boolean
}

fun navGraph(
    action: NavGraphBuilder.() -> Unit,
): NavGraph = NavGraphImpl(NavGraphBuilder().apply(action).build())

private class NavGraphImpl(
    private val graph: Map<KClass<out NavKey>, Set<KClass<out NavKey>>>,
) : NavGraph {
    override fun canNavigate(
        from: NavKey,
        to: NavKey,
    ): Boolean = graph[from::class]?.contains(to::class) == true
}

class NavGraphBuilder {
    private val graph = mutableMapOf<KClass<out NavKey>, Set<KClass<out NavKey>>>()
    internal fun build(): Map<KClass<out NavKey>, Set<KClass<out NavKey>>> = graph.toMap()

    infix fun NavKey.leadsTo(
        destination: NavKey,
    ) {
        val key = this::class
        val value = destination::class
        graph[key] = graph[key]?.let { it + value } ?: setOf(value)
    }

    infix fun NavKey.leadsTo(
        destinations: Iterable<NavKey>,
    ) {
        val key = this::class
        val values = destinations.map { it::class }.toSet()
        graph[key] = graph[key]?.let { it + values } ?: values
    }
}

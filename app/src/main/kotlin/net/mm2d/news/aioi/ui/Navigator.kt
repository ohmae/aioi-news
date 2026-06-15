/*
 * Copyright (c) 2025 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import kotlin.reflect.KClass

@Composable
fun <T : NavKey> rememberNavigator(
    initialKey: T,
    navGraph: NavGraph,
    onExit: () -> Unit,
): Navigator<T> {
    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(initialKey) as NavBackStack<T>
    val currentOnExit by rememberUpdatedState(onExit)
    return remember(navGraph) {
        Navigator(
            backStack = backStack,
            navGraph = navGraph,
            onExit = { currentOnExit() },
        )
    }
}

class Navigator<T : NavKey>(
    val backStack: NavBackStack<T>,
    private val navGraph: NavGraph,
    private val onExit: () -> Unit,
) {
    fun goBack(
        from: T? = null,
    ) {
        if (from != null && backStack.lastOrNull() != from) {
            Log.v("Navigator", "goBack from $from was ignored because current top is ${backStack.lastOrNull()}")
            return
        }
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else {
            onExit()
        }
    }

    fun navigate(
        to: T,
    ) {
        if (backStack.isEmpty()) return
        val from = backStack.last()
        if (from == to) {
            Log.v("Navigator", "from: $from, to: $to is same.")
            return // 連打無効
        }
        if (!navGraph.canNavigate(from, to)) {
            Log.e("Navigator", "from: $from, to: $to is not allowed.")
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
        destination: KClass<out NavKey>,
    ) {
        val key = this::class
        graph[key] = graph[key]?.let { it + destination } ?: setOf(destination)
    }

    infix fun NavKey.leadsTo(
        destinations: Iterable<KClass<out NavKey>>,
    ) {
        val key = this::class
        val values = destinations.toSet()
        graph[key] = graph[key]?.let { it + values } ?: values
    }
}

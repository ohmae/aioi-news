/*
 * Copyright (c) 2025 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import kotlin.reflect.KClass

@Composable
fun <T : NavKey> rememberNavigator(
    initialKey: T,
    navGraph: NavGraph<T>,
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
    private val navGraph: NavGraph<T>,
    private val onExit: () -> Unit,
) {
    private val entryLifecycles = mutableMapOf<T, Lifecycle>()
    private var pendingNavigation: (() -> Unit)? = null
    internal var isNavigationReady by mutableStateOf(false)
        private set

    fun goBack(
        from: T? = null,
    ) {
        navigateBack(from)
    }

    internal fun onSystemBack(
        isPredictiveBack: Boolean,
        popCount: Int = 1,
    ) {
        navigateBack(from = null, requireNavigationReady = !isPredictiveBack, popCount = popCount)
    }

    private fun navigateBack(
        from: T?,
        requireNavigationReady: Boolean = true,
        popCount: Int = 1,
    ) {
        if (popCount <= 0) return
        if (from != null && backStack.lastOrNull() != from) {
            Log.v("Navigator", "goBack from $from was ignored: current top is ${backStack.lastOrNull()}")
            return
        }
        if (requireNavigationReady && !isNavigationReady) {
            enqueueNavigation { navigateBack(from, popCount = popCount) }
            return
        }
        if (backStack.size > 1) {
            // Scene単位の戻る操作は、途中で遷移ガードを再評価せず一括で処理する。
            repeat(popCount.coerceAtMost(backStack.size - 1)) {
                backStack.removeLastOrNull()
            }
            updateNavigationReadiness()
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
        if (!isNavigationReady) {
            enqueueNavigation { navigate(to) }
            return
        }
        backStack.add(to)
        updateNavigationReadiness()
    }

    private fun enqueueNavigation(
        action: () -> Unit,
    ) {
        // 遷移中の操作は先着1件だけ保留する。
        if (pendingNavigation == null) {
            pendingNavigation = action
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun attachLifecycle(
        key: T,
        lifecycle: Lifecycle,
    ) {
        entryLifecycles[key] = lifecycle
        updateNavigationReadiness()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun detachLifecycle(
        key: T,
        lifecycle: Lifecycle,
    ) {
        entryLifecycles.remove(key, lifecycle)
        updateNavigationReadiness()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun updateNavigationReadiness() {
        // 現在の画面がRESUMEDになったら保留していた操作を実行する。
        val current = backStack.lastOrNull()
        isNavigationReady = entryLifecycles[current]?.currentState == Lifecycle.State.RESUMED
        if (isNavigationReady) {
            val action = pendingNavigation
            pendingNavigation = null
            action?.invoke()
        }
    }

    context(entryProviderScope: EntryProviderScope<T>)
    internal inline fun <reified K : T> entry(
        noinline content: @Composable (K) -> Unit,
    ) {
        entryProviderScope.entry<K> { key ->
            // 各entryのライフサイクルを監視
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(this@Navigator, key, lifecycle) {
                attachLifecycle(key, lifecycle)
                val observer = LifecycleEventObserver { _, _ ->
                    updateNavigationReadiness()
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                    detachLifecycle(key, lifecycle)
                }
            }
            content(key)
        }
    }
}

interface NavGraph<T : NavKey> {
    fun canNavigate(
        from: T,
        to: T,
    ): Boolean
}

fun <T : NavKey> navGraph(
    action: NavGraphBuilder<T>.() -> Unit,
): NavGraph<T> = NavGraphImpl(NavGraphBuilder<T>().apply(action).build())

private class NavGraphImpl<T : NavKey>(
    private val graph: Map<KClass<out T>, Set<KClass<out T>>>,
) : NavGraph<T> {
    override fun canNavigate(
        from: T,
        to: T,
    ): Boolean = graph[from::class]?.contains(to::class) == true
}

class NavGraphBuilder<T : NavKey> {
    private val graph = mutableMapOf<KClass<out T>, Set<KClass<out T>>>()
    internal fun build(): Map<KClass<out T>, Set<KClass<out T>>> = graph.toMap()

    infix fun KClass<out T>.leadsTo(
        destination: KClass<out T>,
    ) {
        val key = this
        graph[key] = graph[key]?.let { it + destination } ?: setOf(destination)
    }

    infix fun KClass<out T>.leadsTo(
        destinations: Set<KClass<out T>>,
    ) {
        val key = this
        graph[key] = graph[key]?.let { it + destinations } ?: destinations
    }
}

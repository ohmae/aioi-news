/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import net.mm2d.news.aioi.ui.theme.NavigationSpec
import net.mm2d.news.aioi.ui.theme.PredictiveTransitionSpec
import net.mm2d.news.aioi.ui.theme.TransitionSpec

@Composable
fun <T : NavKey> NavigationDisplay(
    navigator: Navigator<T>,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    entryDecorators: List<NavEntryDecorator<T>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
    sceneStrategies: List<SceneStrategy<T>> = listOf(SinglePaneSceneStrategy()),
    sceneDecoratorStrategies: List<SceneDecoratorStrategy<T>> = emptyList(),
    sharedTransitionScope: SharedTransitionScope? = null,
    sizeTransform: SizeTransform? = null,
    transitionSpec: TransitionSpec<T> = NavigationSpec.push(),
    popTransitionSpec: TransitionSpec<T> = NavigationSpec.pop(),
    predictivePopTransitionSpec: PredictiveTransitionSpec<T> = NavigationSpec.predictivePop(),
    entryProvider: (key: T) -> NavEntry<T>,
) {
    // 画面ごとの保存可能な状態とViewModel、Navigatorのライフサイクル監視をEntryに紐付ける。
    val navigatorDecorator = rememberNavigatorNavEntryDecorator(navigator)
    val allDecorators = remember(entryDecorators, navigatorDecorator) {
        entryDecorators + navigatorDecorator
    }
    val entries = rememberDecoratedNavEntries(
        backStack = navigator.backStack,
        entryDecorators = allDecorators,
        entryProvider = entryProvider,
    )
    // Transition中の画面遷移は無効化するが、
    // 予測型戻るのジェスチャー中も画面遷移判定となるため、この間は戻る操作を許可する必要がある
    var hasPredictiveBackStarted by remember { mutableStateOf(false) }
    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = sceneStrategies,
        sceneDecoratorStrategies = sceneDecoratorStrategies,
        sharedTransitionScope = sharedTransitionScope,
        onBack = {
            navigator.onSystemBack(hasPredictiveBackStarted)
        },
    )

    val scene = sceneState.currentScene
    // 戻るジェスチャーのプレビューが、実際の表示と同じScene履歴を参照するようにする。
    val navigationEventState = rememberNavigationEventState(
        currentInfo = SceneInfo(scene),
        backInfo = sceneState.previousScenes.map { SceneInfo(it) },
    )
    val isPredictiveBackInProgress = navigationEventState.isPredictiveBackInProgress()
    // Compositionが反映された後に開始を記録する。進行状態が終了しても、ここでは記録を消さない。
    SideEffect(isPredictiveBackInProgress) {
        if (isPredictiveBackInProgress) {
            hasPredictiveBackStarted = true
        }
    }
    // Transition中の戻る操作を無効化させる。
    // ただし、予測型戻るのジェスチャー中はTransition中だが、無効化してしまうと戻る操作が確定できなくなる。
    val isBackNavigationEnabled =
        scene.previousEntries.isNotEmpty() &&
            (navigator.isNavigationReady || isPredictiveBackInProgress)

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = isBackNavigationEnabled,
        onBackCancelled = {
            hasPredictiveBackStarted = false
        },
        onBackCompleted = {
            // 次の操作に判定を持ち越さず、今回の戻る処理には開始時の記録を渡す。
            val wasPredictiveBack = hasPredictiveBackStarted
            hasPredictiveBackStarted = false
            // Sceneは複数のEntryを含み得るため、1件固定ではなく戻り先との差分だけスタックを取り除く。
            navigator.onSystemBack(
                isPredictiveBack = wasPredictiveBack,
                popCount = entries.size - scene.previousEntries.size,
            )
        },
    )

    // 画面遷移中Navigationの戻るを無効しているため、戻るイベントがActivityにするのを防ぐ。
    BackHandler(enabled = !navigator.isNavigationReady, onBack = {})
    // 表示と戻るハンドラーで同じ状態を共有し、ジェスチャーの進行に合わせて遷移を描画する。
    NavDisplay(
        sceneState = sceneState,
        navigationEventState = navigationEventState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        sizeTransform = sizeTransform,
        transitionSpec = transitionSpec,
        popTransitionSpec = popTransitionSpec,
        predictivePopTransitionSpec = predictivePopTransitionSpec,
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun NavigationEventState<*>.isPredictiveBackInProgress(): Boolean {
    val state = transitionState
    if (state !is NavigationEventTransitionState.InProgress) return false
    // 前方への遷移を、Navigatorの遷移中ガードを解除できる予測型戻るとして扱わない。
    return state.direction == NavigationEventTransitionState.TRANSITIONING_BACK
}

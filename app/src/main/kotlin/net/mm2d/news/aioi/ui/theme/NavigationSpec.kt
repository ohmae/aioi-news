package net.mm2d.news.aioi.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEvent.SwipeEdge

@OptIn(ExperimentalAnimationApi::class)
object NavigationSpec {
    fun <T : Any> push(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
        {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    initialOffsetX = { it },
                ),
                initialContentExit = slideOutHorizontally(
                    targetOffsetX = { -it / 5 },
                ) + veilOut(),
            )
        }

    fun <T : Any> pop(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
        {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    initialOffsetX = { -it / 5 },
                ) + unveilIn(),
                initialContentExit = slideOutHorizontally(
                    targetOffsetX = { it },
                ),
            )
        }

    fun <T : Any> predictivePop(): AnimatedContentTransitionScope<Scene<T>>.(@SwipeEdge Int) -> ContentTransform =
        { edge ->
            if (edge == NavigationEvent.EDGE_RIGHT) {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { it / 5 },
                    ) + unveilIn(),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { -it },
                    ),
                )
            } else {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { -it / 5 },
                    ) + unveilIn(),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { it },
                    ),
                )
            }
        }
}

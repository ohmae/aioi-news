package net.mm2d.news.aioi.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

object NavigationSpec {
    fun <T : Any> transition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
        {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    initialOffsetX = { it },
                ),
                initialContentExit = slideOutHorizontally(
                    targetOffsetX = { -it / 5 },
                ),
            )
        }

    fun <T : Any> popTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
        {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
                    initialOffsetX = { -it / 5 },
                ),
                initialContentExit = slideOutHorizontally(
                    targetOffsetX = { it },
                ),
            )
        }

    fun <T : Any> predictivePopTransition(): AnimatedContentTransitionScope<Scene<T>>.(
        @NavigationEvent.SwipeEdge Int,
    ) -> ContentTransform =
        { edge ->
            if (edge == NavigationEvent.EDGE_RIGHT) {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { it / 5 },
                    ),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { -it },
                    ),
                )
            } else {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { -it / 5 },
                    ),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { it },
                    ),
                )
            }
        }
}

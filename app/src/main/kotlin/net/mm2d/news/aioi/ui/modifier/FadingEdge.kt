/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.ui.modifier

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawVerticalFadingEdges(
    scrollableState: ScrollableState,
    edgeLength: Dp = 64.dp,
) = then(
    Modifier
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            val edgeLengthPx = edgeLength.toPx()
            val backwardBrush = Brush.verticalGradient(
                colors = listOf(Color(0x40000000), Color.Black),
                startY = 0f,
                endY = edgeLengthPx,
            )
            val forwardBrush = Brush.verticalGradient(
                colors = listOf(Color.Black, Color(0x40000000)),
                startY = size.height - edgeLengthPx,
                endY = size.height,
            )
            onDrawWithContent {
                drawContent()
                if (scrollableState.canScrollBackward) {
                    drawRect(
                        brush = backwardBrush,
                        blendMode = BlendMode.DstIn,
                    )
                }
                if (scrollableState.canScrollForward) {
                    drawRect(
                        brush = forwardBrush,
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        },
)

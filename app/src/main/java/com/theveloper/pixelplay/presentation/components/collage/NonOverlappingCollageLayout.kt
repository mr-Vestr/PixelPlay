package com.theveloper.pixelplay.presentation.components.collage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * A custom layout that arranges its children in a collage-like fashion,
 * programmatically ensuring that no two children overlap.
 *
 * It places items one by one, starting from the center and spiraling outwards,
 * looking for the first available spot that doesn't conflict with already placed items.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param spacing The minimum spacing to maintain between each placed item.
 * @param content The children composables to be laid out.
 */
@Composable
fun NonOverlappingCollageLayout(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            return@Layout layout(constraints.minWidth, constraints.minHeight) {}
        }

        val spacingPx = spacing.toPx().toInt()
        val placeables = measurables.map { it.measure(Constraints()) }

        val placedItems = mutableListOf<Pair<Placeable, Rect>>()
        var totalWidth = 0
        var totalHeight = 0

        // Heuristic: Place larger items first, as they are harder to fit in later.
        val sortedPlaceables = placeables.sortedByDescending { it.width * it.height }

        sortedPlaceables.forEach { placeable ->
            var bestRect: Rect? = null
            var placed = false

            // Spiral placement algorithm to find a non-overlapping spot
            var radius = 0
            var angle = 0.0
            val spiralStep = 10.0

            while (!placed) {
                // Determine candidate position (x, y) on a spiral from the center
                val centerX = (constraints.maxWidth / 2) + (radius * kotlin.math.cos(angle)).toInt()
                val centerY = (constraints.maxHeight / 2) + (radius * kotlin.math.sin(angle)).toInt()

                val candidateX = centerX - placeable.width / 2
                val candidateY = centerY - placeable.height / 2

                val candidateRect = Rect(
                    left = candidateX.toFloat(),
                    top = candidateY.toFloat(),
                    right = (candidateX + placeable.width).toFloat(),
                    bottom = (candidateY + placeable.height).toFloat()
                )

                // Check for overlaps with already placed items, considering spacing
                val overlaps = placedItems.any { (_, placedRect) ->
                    val inflatedRect = placedRect.inflate(spacingPx.toFloat())
                    inflatedRect.overlaps(candidateRect)
                }

                // Check if the candidate is within the layout bounds
                val inBounds = candidateRect.left >= 0 &&
                        candidateRect.top >= 0 &&
                        candidateRect.right <= constraints.maxWidth &&
                        candidateRect.bottom <= constraints.maxHeight

                if (!overlaps && inBounds) {
                    bestRect = candidateRect
                    placed = true
                } else {
                    // Move to the next point on the spiral
                    angle += spiralStep
                    if (angle > 360) {
                        angle = 0.0
                        radius += (spacingPx * 0.5).toInt().coerceAtLeast(1)
                    }
                }

                // Failsafe to prevent an infinite loop if an item is too large
                if (radius > max(constraints.maxWidth, constraints.maxHeight)) {
                    // Place it at the center as a last resort
                    val fallbackX = (constraints.maxWidth - placeable.width) / 2
                    val fallbackY = (constraints.maxHeight - placeable.height) / 2
                    bestRect = Rect(
                        left = fallbackX.toFloat(),
                        top = fallbackY.toFloat(),
                        right = (fallbackX + placeable.width).toFloat(),
                        bottom = (fallbackY + placeable.height).toFloat()
                    )
                    placed = true
                }
            }

            bestRect?.let {
                placedItems.add(placeable to it)
                totalWidth = max(totalWidth, it.right.toInt())
                totalHeight = max(totalHeight, it.bottom.toInt())
            }
        }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            placedItems.forEach { (placeable, rect) ->
                placeable.placeRelative(rect.left.toInt(), rect.top.toInt())
            }
        }
    }
}
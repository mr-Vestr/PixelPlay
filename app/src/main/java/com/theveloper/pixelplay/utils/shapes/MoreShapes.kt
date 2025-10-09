package com.theveloper.pixelplay.utils.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sqrt

/**
 * A squircle shape.
 */
class SquircleShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rounded(
            androidx.compose.ui.geometry.RoundRect(
                rect = Rect(0f, 0f, size.width, size.height),
                cornerRadius = cornerRadius,
                cornerRadiusY = cornerRadius
            )
        )
    }
}

/**
 * A four-leaf clover shape.
 */
class CloverShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val radius = size.minDimension / 2f
        val center = size.width / 2f
        path.moveTo(center, center)
        path.addOval(Rect(center - radius, center - radius, center, center))
        path.addOval(Rect(center, center - radius, center + radius, center))
        path.addOval(Rect(center - radius, center, center, center + radius))
        path.addOval(Rect(center, center, center + radius, center + radius))
        return Outline.Generic(path)
    }
}

/**
 * A ticket shape.
 */
class TicketShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            reset()
            // TOP LEFT ARC
            arcTo(
                rect = Rect(
                    left = -cornerRadius,
                    top = -cornerRadius,
                    right = cornerRadius,
                    bottom = cornerRadius
                ),
                startAngleDegrees = 90.0f,
                sweepAngleDegrees = -90.0f,
                forceMoveTo = false
            )
            lineTo(x = size.width - cornerRadius, y = 0f)
            // TOP RIGHT ARC
            arcTo(
                rect = Rect(
                    left = size.width - cornerRadius,
                    top = -cornerRadius,
                    right = size.width + cornerRadius,
                    bottom = cornerRadius
                ),
                startAngleDegrees = 180.0f,
                sweepAngleDegrees = -90.0f,
                forceMoveTo = false
            )
            lineTo(x = size.width, y = size.height - cornerRadius)
            // BOTTOM RIGHT ARC
            arcTo(
                rect = Rect(
                    left = size.width - cornerRadius,
                    top = size.height - cornerRadius,
                    right = size.width + cornerRadius,
                    bottom = size.height + cornerRadius
                ),
                startAngleDegrees = 270.0f,
                sweepAngleDegrees = -90.0f,
                forceMoveTo = false
            )
            lineTo(x = cornerRadius, y = size.height)
            // BOTTOM LEFT ARC
            arcTo(
                rect = Rect(
                    left = -cornerRadius,
                    top = size.height - cornerRadius,
                    right = cornerRadius,
                    bottom = size.height + cornerRadius
                ),
                startAngleDegrees = 0.0f,
                sweepAngleDegrees = -90.0f,
                forceMoveTo = false
            )
            lineTo(x = 0f, y = cornerRadius)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A gem shape.
 */
class GemShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            moveTo(width * 0.5f, 0f)
            lineTo(width, height * 0.33f)
            lineTo(width, height * 0.66f)
            lineTo(width * 0.5f, height)
            lineTo(0f, height * 0.66f)
            lineTo(0f, height * 0.33f)
            close()
        }
        return Outline.Generic(path)
    }
}
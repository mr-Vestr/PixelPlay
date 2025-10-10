package com.theveloper.pixelplay.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theveloper.pixelplay.utils.shapes.RoundedStarShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Defines the structure of an item within the collage.
 * Uses relative sizes and offsets to make the pattern responsive.
 *
 * @param shape The `Shape` to be used for clipping the content.
 * @param relativeOffset The position (x, y) as a fraction of the container size (0.0f to 1.0f).
 * @param relativeSize The size (width, height) as a fraction of the container size.
 * @param rotation Degrees of rotation for the item.
 * @param content The Composable content to be displayed within the shape.
 */
data class PatternItem(
    val shape: Shape,
    val relativeOffset: Offset,
    val relativeSize: Size,
    val rotation: Float = 0f,
    val content: @Composable BoxScope.() -> Unit
)

/**
 * A Composable that arranges a list of `PatternItem` into a collage.
 * They do not overlap because their positions and sizes are predefined in the patterns.
 *
 * @param pattern The list of `PatternItem` to draw.
 * @param modifier The modifier for this composable.
 */
@Composable
fun TangramCollage(
    pattern: List<PatternItem>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight

        pattern.forEach { item ->
            val itemWidth = maxWidth * item.relativeSize.width
            val itemHeight = maxHeight * item.relativeSize.height

            Box(
                modifier = Modifier
                    .offset(
                        x = maxWidth * item.relativeOffset.x,
                        y = maxHeight * item.relativeOffset.y
                    )
                    .size(width = itemWidth, height = itemHeight)
                    .rotate(item.rotation)
                    .clip(item.shape)
            ) {
                item.content(this)
            }
        }
    }
}

// --- Dynamic Shape Classes ---

/**
 * Shape describing Polygons
 *
 * Note: The shape draws within the minimum of provided width and height so can't be used to create stretched shape.
 *
 * @param sides number of sides.
 * @param rotation value between 0 - 360
 */
class PolygonShape(private val sides: Int, private val rotation: Float = 0f) : Shape {

    private companion object {
        const val TWO_PI = 2 * PI
    }

    private val stepCount = ((TWO_PI) / sides)
    private val rotationDegree = (PI / 180) * rotation

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(Path().apply {
        val r = min(size.height, size.width) * .5f
        val xCenter = size.width * .5f
        val yCenter = size.height * .5f

        moveTo(xCenter, yCenter)

        var t = -rotationDegree
        var i = 0
        while (i <= sides) {
            val x = r * cos(t)
            val y = r * sin(t)
            lineTo((x + xCenter).toFloat(), (y + yCenter).toFloat())
            t += stepCount
            i++
        }
        close()
    })
}

// --- Material 3 Expressive Custom Shapes ---

object MaterialExpressiveShapes {
    val Scallop = RoundedStarShape(sides = 8, curve = 0.15)
    val Pentagon = PolygonShape(sides = 5, rotation = -90f)
    val AsymmetricStar = RoundedStarShape(sides = 5, curve = 0.5, rotation = -90f)
    val SoftSquare = GenericShape { size, _ ->
        val cornerRadius = size.minDimension * 0.3f
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
        )
    }
}


// --- Example Content for Shapes ---

@Composable
fun ImageContent(url: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun ColorContent(color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(color))
}


// --- Collage Pattern Definitions ---

object CollagePatterns {

    private val placeholderUrls = listOf(
        "https://picsum.photos/seed/abstract/400/600",
        "https://picsum.photos/seed/architecture/400/400",
        "https://picsum.photos/seed/nature/400/400",
        "https://picsum.photos/seed/technology/400/400",
        "https://picsum.photos/seed/people/400/400",
        "https://picsum.photos/seed/food/400/400"
    )

    /**
     * Pattern 1: Based on the user's screenshot. Asymmetric and centered.
     */
    val homescreenPattern: List<PatternItem> = listOf(
        PatternItem(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(60.dp), // Pill shape
            relativeOffset = Offset(0.25f, 0.25f),
            relativeSize = Size(0.5f, 0.4f),
            rotation = -15f,
        ) { ImageContent(placeholderUrls[0]) },
        PatternItem(
            shape = CircleShape,
            relativeOffset = Offset(0.05f, 0.22f),
            relativeSize = Size(0.25f, 0.2f)
        ) { ImageContent(placeholderUrls[1]) },
        PatternItem(
            shape = CircleShape,
            relativeOffset = Offset(0.7f, 0.45f),
            relativeSize = Size(0.25f, 0.2f),
        ) { ImageContent(placeholderUrls[2]) },
        PatternItem(
            shape = MaterialExpressiveShapes.SoftSquare,
            relativeOffset = Offset(0.08f, 0.65f),
            relativeSize = Size(0.4f, 0.3f),
            rotation = 10f
        ) { ImageContent(placeholderUrls[3]) },
        PatternItem(
            shape = MaterialExpressiveShapes.Scallop,
            relativeOffset = Offset(0.55f, 0.7f),
            relativeSize = Size(0.35f, 0.28f),
            rotation = -10f
        ) { ImageContent(placeholderUrls[4]) }
    )

    /**
     * Pattern 2: A balanced, symmetrical design.
     */
    val symmetricalPattern: List<PatternItem> = listOf(
        PatternItem(
            shape = MaterialExpressiveShapes.Pentagon,
            relativeOffset = Offset(0.3f, 0.35f),
            relativeSize = Size(0.4f, 0.35f)
        ) { ImageContent(placeholderUrls[0]) },
        PatternItem(
            shape = MaterialExpressiveShapes.SoftSquare,
            relativeOffset = Offset(0.05f, 0.05f),
            relativeSize = Size(0.25f, 0.22f),
            rotation = -15f
        ) { ColorContent(MaterialTheme.colorScheme.secondaryContainer) },
        PatternItem(
            shape = MaterialExpressiveShapes.SoftSquare,
            relativeOffset = Offset(0.7f, 0.05f),
            relativeSize = Size(0.25f, 0.22f),
            rotation = 15f
        ) { ColorContent(MaterialTheme.colorScheme.secondaryContainer) },
        PatternItem(
            shape = MaterialExpressiveShapes.SoftSquare,
            relativeOffset = Offset(0.05f, 0.73f),
            relativeSize = Size(0.25f, 0.22f),
            rotation = 15f
        ) { ColorContent(MaterialTheme.colorScheme.tertiaryContainer) },
        PatternItem(
            shape = MaterialExpressiveShapes.SoftSquare,
            relativeOffset = Offset(0.7f, 0.73f),
            relativeSize = Size(0.25f, 0.22f),
            rotation = -15f
        ) { ColorContent(MaterialTheme.colorScheme.tertiaryContainer) },
    )

    /**
     * Pattern 3: A dynamic design that flows from one corner to another.
     */
    val dynamicFlowPattern: List<PatternItem> = listOf(
        PatternItem(
            shape = MaterialExpressiveShapes.AsymmetricStar,
            relativeOffset = Offset(0.55f, 0.1f),
            relativeSize = Size(0.4f, 0.35f),
            rotation = 25f
        ) { ImageContent(placeholderUrls[1]) },
        PatternItem(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            relativeOffset = Offset(0.3f, 0.3f),
            relativeSize = Size(0.4f, 0.3f),
            rotation = 15f
        ) { ImageContent(placeholderUrls[2]) },
        PatternItem(
            shape = MaterialExpressiveShapes.Scallop,
            relativeOffset = Offset(0.1f, 0.5f),
            relativeSize = Size(0.35f, 0.28f),
            rotation = 5f
        ) { ImageContent(placeholderUrls[3]) },
        PatternItem(
            shape = CircleShape,
            relativeOffset = Offset(0.5f, 0.65f),
            relativeSize = Size(0.3f, 0.25f)
        ) { ImageContent(placeholderUrls[4]) }
    )

    /**
     * Pattern 4: Using the new dynamic shapes.
     */
    val expressiveShapesPattern: List<PatternItem> = listOf(
        PatternItem(
            shape = RoundedStarShape(sides = 7, curve = 0.25),
            relativeOffset = Offset(0.3f, 0.05f),
            relativeSize = Size(0.4f, 0.35f),
            rotation = 15f
        ) { ImageContent(placeholderUrls[0]) },
        PatternItem(
            shape = PolygonShape(sides = 3, rotation = 0f), // Triangle
            relativeOffset = Offset(0.05f, 0.3f),
            relativeSize = Size(0.3f, 0.25f),
            rotation = -25f
        ) { ColorContent(MaterialTheme.colorScheme.primaryContainer) },
        PatternItem(
            shape = RoundedStarShape(sides = 4, curve = 0.8, rotation = 45f), // Shuriken/Star
            relativeOffset = Offset(0.65f, 0.4f),
            relativeSize = Size(0.3f, 0.25f),
            rotation = 20f
        ) { ImageContent(placeholderUrls[2]) },
        PatternItem(
            shape = PolygonShape(sides = 6), // Hexagon
            relativeOffset = Offset(0.2f, 0.6f),
            relativeSize = Size(0.4f, 0.35f),
            rotation = 0f
        ) { ImageContent(placeholderUrls[3]) },
        PatternItem(
            shape = RoundedStarShape(sides = 12, curve = 0.1), // Flower
            relativeOffset = Offset(0.6f, 0.7f),
            relativeSize = Size(0.3f, 0.25f),
            rotation = 0f
        ) { ColorContent(MaterialTheme.colorScheme.tertiaryContainer) }
    )

    val allPatterns = mapOf(
        "homescreen" to homescreenPattern,
        "symmetrical" to symmetricalPattern,
        "dynamic_flow" to dynamicFlowPattern,
        "expressive" to expressiveShapesPattern
    )

    fun getRandomPattern(): List<PatternItem> {
        return allPatterns.values.random()
    }
}

@Composable
fun AlbumTangramCollage(
    songs: kotlinx.collections.immutable.ImmutableList<com.theveloper.pixelplay.data.model.Song>,
    patternName: String,
    modifier: Modifier = Modifier,
    onSongClick: (com.theveloper.pixelplay.data.model.Song) -> Unit,
) {
    val pattern = androidx.compose.runtime.remember(patternName) {
        when (patternName) {
            "random" -> CollagePatterns.getRandomPattern()
            else -> CollagePatterns.allPatterns[patternName] ?: CollagePatterns.getRandomPattern()
        }
    }

    val itemsWithContent = androidx.compose.runtime.remember(pattern, songs) {
        if (songs.isEmpty()) {
            emptyList()
        } else {
            pattern.mapIndexed { index, item ->
                val song = songs[index % songs.size]
                item.copy(content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { onSongClick(song) }
                    ) {
                        ImageContent(url = song.albumArtUriString ?: "")
                    }
                })
            }
        }
    }

    if (songs.isNotEmpty()) {
        TangramCollage(
            pattern = itemsWithContent,
            modifier = modifier
        )
    }
}
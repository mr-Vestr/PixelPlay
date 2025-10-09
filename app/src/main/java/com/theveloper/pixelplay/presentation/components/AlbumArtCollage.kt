package com.theveloper.pixelplay.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.utils.shapes.CloverShape
import com.theveloper.pixelplay.utils.shapes.GemShape
import com.theveloper.pixelplay.utils.shapes.RoundedStarShape
import com.theveloper.pixelplay.utils.shapes.TicketShape
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
data class Config(val size: Dp, val width: Dp, val height: Dp, val align: Alignment, val rot: Float, val shape: Shape, val offsetX: Dp, val offsetY: Dp)

@Composable
private fun rememberCollagePatterns(height: Dp, boxMaxHeight: Dp): List<List<Config>> {
    return remember(height, boxMaxHeight) {
        val min = minOf(300.dp, height)

        val pattern1 = listOf(
            Config(size = min * 0.8f, width = min * 0.48f, height = min * 0.8f, align = Alignment.Center, rot = 45f, shape = RoundedCornerShape(percent = 50), offsetX = 0.dp, offsetY = 0.dp),
            Config(size = min * 0.4f, width = min * 0.24f, height = min * 0.24f, align = Alignment.TopStart, rot = 0f, shape = CircleShape, offsetX = (300.dp * 0.05f), offsetY = (boxMaxHeight * 0.05f)),
            Config(size = min * 0.4f, width = min * 0.24f, height = min * 0.24f, align = Alignment.BottomEnd, rot = 0f, shape = CircleShape, offsetX = -(300.dp * 0.05f), offsetY = -(boxMaxHeight * 0.05f)),
            Config(size = min * 0.6f, width = min * 0.35f, height = min * 0.35f, align = Alignment.TopStart, rot = -20f, shape = RoundedCornerShape(20.dp), offsetX = (300.dp * 0.1f), offsetY = (boxMaxHeight * 0.1f)),
            Config(size = min * 0.9f, width = min * 0.9f, height = min * 0.9f, align = Alignment.BottomEnd, rot = 0f, shape = RoundedStarShape(sides = 6, curve = 0.09, rotation = 45f), offsetX = (42).dp, offsetY = 0.dp)
        )

        val pattern2 = listOf(
            Config(size = min * 0.6f, width = min * 0.6f, height = min * 0.6f, align = Alignment.Center, rot = 0f, shape = CloverShape(), offsetX = 0.dp, offsetY = 0.dp),
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.TopStart, rot = -15f, shape = RoundedStarShape(8, 0.2), offsetX = (min * 0.1f), offsetY = (boxMaxHeight * 0.1f)),
            Config(size = min * 0.4f, width = min * 0.4f, height = min * 0.4f, align = Alignment.TopEnd, rot = 15f, shape = RoundedCornerShape(30.dp), offsetX = -(min * 0.1f), offsetY = (boxMaxHeight * 0.1f)),
            Config(size = min * 0.45f, width = min * 0.45f, height = min * 0.45f, align = Alignment.BottomStart, rot = 15f, shape = CircleShape, offsetX = (min * 0.15f), offsetY = -(boxMaxHeight * 0.15f)),
            Config(size = min * 0.55f, width = min * 0.55f, height = min * 0.55f, align = Alignment.BottomEnd, rot = -15f, shape = RoundedStarShape(5, 0.3), offsetX = -(min * 0.1f), offsetY = -(boxMaxHeight * 0.1f))
        )

        val pattern3 = listOf(
            Config(size = min * 0.7f, width = min * 0.7f, height = min * 0.7f, align = Alignment.Center, rot = 10f, shape = GemShape(), offsetX = 0.dp, offsetY = 0.dp),
            Config(size = min * 0.4f, width = min * 0.25f, height = min * 0.4f, align = Alignment.TopEnd, rot = -10f, shape = TicketShape(20f), offsetX = -(min * 0.1f), offsetY = (boxMaxHeight * 0.15f)),
            Config(size = min * 0.4f, width = min * 0.25f, height = min * 0.4f, align = Alignment.BottomStart, rot = -10f, shape = TicketShape(20f), offsetX = (min * 0.1f), offsetY = -(boxMaxHeight * 0.15f)),
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.TopStart, rot = 25f, shape = RoundedStarShape(4, 0.1, rotation = 45f), offsetX = (min * 0.05f), offsetY = (boxMaxHeight * 0.05f)),
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.BottomEnd, rot = 25f, shape = RoundedStarShape(4, 0.1, rotation = 45f), offsetX = -(min * 0.05f), offsetY = -(boxMaxHeight * 0.05f))
        )

        val pattern4 = listOf(
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.TopStart, rot = 0f, shape = RoundedCornerShape(24.dp), offsetX = (min * 0.1f), offsetY = (boxMaxHeight * 0.1f)),
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.TopEnd, rot = 0f, shape = RoundedCornerShape(24.dp), offsetX = -(min * 0.1f), offsetY = (boxMaxHeight * 0.1f)),
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.BottomStart, rot = 0f, shape = RoundedCornerShape(24.dp), offsetX = (min * 0.1f), offsetY = -(boxMaxHeight * 0.1f)),
            Config(size = min * 0.5f, width = min * 0.5f, height = min * 0.5f, align = Alignment.BottomEnd, rot = 0f, shape = RoundedCornerShape(24.dp), offsetX = -(min * 0.1f), offsetY = -(boxMaxHeight * 0.1f)),
            Config(size = min * 0.4f, width = min * 0.4f, height = min * 0.4f, align = Alignment.Center, rot = 45f, shape = RoundedCornerShape(12.dp), offsetX = 0.dp, offsetY = 0.dp)
        )

        listOf(pattern1, pattern2, pattern3, pattern4)
    }
}


/**
 * Muestra hasta 6 portadas en un layout de collage con formas simplificadas y redondeadas.
 * Las formas se dividen en dos grupos (superior e inferior) para evitar superposición.
 * Incluye una píldora central, círculo, squircle y estrella, con disposición ajustada.
 * Ajusta tamaños, rotaciones y posiciones para crear un look dinámico.
 * Utiliza BoxWithConstraints para adaptar las dimensiones al contenedor.
 */
@Composable
fun AlbumArtCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 400.dp,
    padding: Dp = 0.dp,
    onSongClick: (Song) -> Unit,
) {
    val context = LocalContext.current
    val songsToShow = remember(songs) {
        (songs.take(6) + List(6 - songs.size.coerceAtMost(6)) { null }).toImmutableList()
    }

    val requests = remember(songsToShow) {
        songsToShow.map { song ->
            song?.albumArtUriString?.let {
                ImageRequest.Builder(context)
                    .data(it)
                    .dispatcher(Dispatchers.IO)
                    .crossfade(true)
                    .error(R.drawable.rounded_album_24)
                    .build()
            }
        }.toImmutableList()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(padding)
    ) {
        val boxMaxHeight = maxHeight
        val collagePatterns = rememberCollagePatterns(height = height, boxMaxHeight = boxMaxHeight)
        val randomPattern = remember { collagePatterns.random() }

        val shapeConfigs by produceState<List<Config>>(initialValue = emptyList(), randomPattern) {
            value = withContext(Dispatchers.Default) {
                randomPattern
            }
        }

        if (shapeConfigs.isNotEmpty()) {
            Box(Modifier.fillMaxSize()) {
                shapeConfigs.forEachIndexed { idx, cfg ->
                    songsToShow.getOrNull(idx)?.let { song ->
                        AsyncImage(
                            model = requests.getOrNull(idx),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(cfg.width, cfg.height)
                                .align(cfg.align)
                                .offset(cfg.offsetX, cfg.offsetY)
                                .graphicsLayer { rotationZ = cfg.rot }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSongClick(song) }
                                .background(
                                    shape = cfg.shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clip(cfg.shape)
                        )
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.rounded_music_note_24),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}
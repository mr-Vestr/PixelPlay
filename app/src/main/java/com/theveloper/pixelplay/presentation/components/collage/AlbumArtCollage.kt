package com.theveloper.pixelplay.presentation.components.collage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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

@Stable
data class CollageItemConfig(val shape: Shape, val size: Dp, val rotation: Float = 0f)

@Composable
private fun rememberCollagePatterns(baseSize: Dp): List<List<CollageItemConfig>> {
    return remember(baseSize) {
        val pattern1 = listOf(
            CollageItemConfig(shape = RoundedCornerShape(percent = 50), size = baseSize * 0.7f, rotation = 45f),
            CollageItemConfig(shape = CircleShape, size = baseSize * 0.4f),
            CollageItemConfig(shape = RoundedStarShape(sides = 6, curve = 0.09f, rotation = 45f), size = baseSize * 0.5f),
            CollageItemConfig(shape = CircleShape, size = baseSize * 0.4f),
            CollageItemConfig(shape = RoundedCornerShape(20.dp), size = baseSize * 0.45f, rotation = -20f),
        )

        val pattern2 = listOf(
            CollageItemConfig(shape = CloverShape(), size = baseSize * 0.6f),
            CollageItemConfig(shape = RoundedStarShape(8, 0.2f), size = baseSize * 0.45f, rotation = -15f),
            CollageItemConfig(shape = RoundedCornerShape(30.dp), size = baseSize * 0.4f, rotation = 15f),
            CollageItemConfig(shape = CircleShape, size = baseSize * 0.35f, rotation = 15f),
            CollageItemConfig(shape = RoundedStarShape(5, 0.3f), size = baseSize * 0.5f, rotation = -15f)
        )

        val pattern3 = listOf(
            CollageItemConfig(shape = GemShape(), size = baseSize * 0.65f, rotation = 10f),
            CollageItemConfig(shape = TicketShape(20f), size = baseSize * 0.5f, rotation = -10f),
            CollageItemConfig(shape = TicketShape(20f), size = baseSize * 0.5f, rotation = -10f),
            CollageItemConfig(shape = RoundedStarShape(4, 0.1f, rotation = 45f), size = baseSize * 0.4f, rotation = 25f),
            CollageItemConfig(shape = RoundedStarShape(4, 0.1f, rotation = 45f), size = baseSize * 0.4f, rotation = 25f)
        )

        val pattern4 = listOf(
            CollageItemConfig(shape = RoundedCornerShape(24.dp), size = baseSize * 0.45f),
            CollageItemConfig(shape = RoundedCornerShape(24.dp), size = baseSize * 0.45f),
            CollageItemConfig(shape = RoundedCornerShape(24.dp), size = baseSize * 0.45f),
            CollageItemConfig(shape = RoundedCornerShape(24.dp), size = baseSize * 0.45f),
            CollageItemConfig(shape = RoundedCornerShape(12.dp), size = baseSize * 0.35f, rotation = 45f)
        )

        listOf(pattern1, pattern2, pattern3, pattern4)
    }
}

@Composable
fun AlbumArtCollage(
    songs: ImmutableList<Song>,
    modifier: Modifier = Modifier,
    height: Dp = 400.dp,
    padding: Dp = 0.dp,
    spacing: Dp = 8.dp,
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
        val baseSize = minOf(maxWidth, maxHeight)
        val collagePatterns = rememberCollagePatterns(baseSize = baseSize)
        val randomPattern = remember { collagePatterns.random() }

        if (songs.isNotEmpty()) {
            NonOverlappingCollageLayout(
                modifier = Modifier.fillMaxSize(),
                spacing = spacing
            ) {
                songsToShow.forEachIndexed { index, song ->
                    if (song != null) {
                        val config = randomPattern.getOrNull(index) ?: randomPattern.first()
                        AsyncImage(
                            model = requests.getOrNull(index),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(config.size)
                                .graphicsLayer { rotationZ = config.rotation }
                                .clip(config.shape)
                                .background(
                                    shape = config.shape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSongClick(song) }
                        )
                    }
                }
            }
        } else {
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
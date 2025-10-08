package com.theveloper.pixelplay.presentation.components

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.size.Size
import com.theveloper.pixelplay.data.model.Song
import kotlinx.collections.immutable.ImmutableList

// ====== TIPOS/STATE DEL CARRUSEL (wrapper para mantener compatibilidad) ======

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberRoundedParallaxCarouselState(
    initialPage: Int,
    pageCount: () -> Int
): CarouselState = rememberCarouselState(initialItem = initialPage, itemCount = pageCount)

// ====== TU SECCIÓN: ACOPLADA AL NUEVO API ======

private data class CarouselConfig(
    val preferredItemWidth: Dp,
    val contentPadding: PaddingValues,
    val carouselHeight: Dp,
    val isScrollEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumCarouselSection(
    currentSong: Song?,
    queue: ImmutableList<Song>,
    expansionFraction: Float,
    onSongSelected: (Song) -> Unit,
    modifier: Modifier = Modifier,
    carouselStyle: String = CarouselStyle.ONE_PEEK,
    itemSpacing: Dp = 8.dp
) {
    if (queue.isEmpty()) return

    val carouselState = rememberCarouselState(
        initialPage = queue.indexOf(currentSong).coerceAtLeast(0),
        pageCount = { queue.size }
    )

    val currentSongIndex by remember {
        derivedStateOf {
            queue.indexOf(currentSong).coerceAtLeast(0)
        }
    }

    LaunchedEffect(currentSongIndex, queue.size) {
        if (carouselState.currentPage != currentSongIndex) {
            carouselState.animateScrollToItem(currentSongIndex)
        }
    }

    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(carouselState) {
        snapshotFlow { carouselState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it && carouselState.currentPage != currentSongIndex }
            .collect {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                queue.getOrNull(carouselState.currentPage)?.let(onSongSelected)
            }
    }

    val corner = lerp(16.dp, 4.dp, expansionFraction.coerceIn(0f, 1f))

    BoxWithConstraints(modifier = modifier) {
        val availableWidth = this.maxWidth

        val config = when (carouselStyle) {
            CarouselStyle.NO_PEEK -> {
                val itemWidth = availableWidth * 0.85f
                val padding = (availableWidth - itemWidth) / 2
                CarouselConfig(itemWidth, PaddingValues(horizontal = padding), itemWidth, true)
            }
            else -> { // Default to One Peek
                val itemWidth = availableWidth * 0.8f
                CarouselConfig(itemWidth, PaddingValues(horizontal = (availableWidth * 0.1f)), itemWidth, true)
            }
        }

        RoundedHorizontalMultiBrowseCarousel(
            state = carouselState,
            modifier = Modifier.height(config.carouselHeight),
            itemSpacing = itemSpacing,
            itemCornerRadius = corner,
            contentPadding = config.contentPadding,
            preferredItemWidth = config.preferredItemWidth,
            userScrollEnabled = config.isScrollEnabled,
            carouselStyle = carouselStyle
        ) { index ->
            val song = queue[index]
            Box(Modifier.fillMaxSize().aspectRatio(1f)) {
                OptimizedAlbumArt(
                    uri = song.albumArtUriString,
                    title = song.title,
                    modifier = Modifier.fillMaxSize(),
                    targetSize = Size(600, 600)
                )
            }
        }
    }
}
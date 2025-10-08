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
import com.theveloper.pixelplay.data.preferences.CarouselStyle
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumCarouselSection(
    currentSong: Song?,
    queue: ImmutableList<Song>,
    expansionFraction: Float,
    onSongSelected: (Song) -> Unit,
    modifier: Modifier = Modifier,
    carouselStyle: String = CarouselStyle.ONE_PEEK,
    itemSpacing: Dp = 8.dp,
    playerViewModel: PlayerViewModel
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

        val preferredItemWidth: Dp
        val contentPadding: PaddingValues
        val carouselAlignment: CarouselAlignment

        when (carouselStyle) {
            CarouselStyle.NO_PEEK -> {
                preferredItemWidth = availableWidth
                contentPadding = PaddingValues(0.dp)
                carouselAlignment = CarouselAlignment.Center
            }
            CarouselStyle.ONE_PEEK -> {
                preferredItemWidth = availableWidth * 0.8f
                contentPadding = PaddingValues(horizontal = availableWidth * 0.1f)
                carouselAlignment = CarouselAlignment.Start
            }
            CarouselStyle.TWO_PEEK -> {
                preferredItemWidth = availableWidth * 0.7f
                contentPadding = PaddingValues(horizontal = (availableWidth * 0.15f))
                carouselAlignment = CarouselAlignment.Center
            }
            else -> { // Default to One Peek
                preferredItemWidth = availableWidth * 0.8f
                contentPadding = PaddingValues(horizontal = availableWidth * 0.1f)
                carouselAlignment = CarouselAlignment.Start
            }
        }

        RoundedHorizontalMultiBrowseCarousel(
            state = carouselState,
            modifier = Modifier.fillMaxSize(),
            itemSpacing = itemSpacing,
            itemCornerRadius = corner,
            contentPadding = contentPadding,
            preferredItemWidth = preferredItemWidth
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
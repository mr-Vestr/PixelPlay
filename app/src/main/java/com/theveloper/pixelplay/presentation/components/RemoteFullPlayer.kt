package com.theveloper.pixelplay.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.media3.common.util.UnstableApi
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import kotlinx.collections.immutable.ImmutableList

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteFullPlayer(
    currentSong: Song?,
    currentPosition: Long,
    totalDuration: Long,
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onSeekFinished: (Long) -> Unit,
    onCollapse: () -> Unit,
    onShowCastClicked: () -> Unit,
    onShowQueueClicked: () -> Unit,
    expansionFraction: Float,
    queue: ImmutableList<Song>,
    onSongSelectedFromCarousel: (Song) -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                modifier = Modifier.alpha(expansionFraction.coerceIn(0f, 1f)),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = { /* Title can be empty or show device name */ },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(42.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .clickable(onClick = onCollapse),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_keyboard_arrow_down_24),
                                contentDescription = "Collapse",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(height = 42.dp, width = 50.dp)
                                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 6.dp, bottomStart = 50.dp, bottomEnd = 6.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .clickable { onShowCastClicked() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_cast_connected_24),
                                contentDescription = "Cast Options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(height = 42.dp, width = 50.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 50.dp, bottomStart = 6.dp, bottomEnd = 50.dp))
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .clickable(onClick = onShowQueueClicked),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_queue_music_24),
                                contentDescription = "Queue",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = lerp(8.dp, 24.dp, expansionFraction),
                    vertical = lerp(0.dp, 16.dp, expansionFraction)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if (currentSong != null) {
                val albumArtContainerModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = lerp(4.dp, 8.dp, expansionFraction))
                    .height(lerp(150.dp, 260.dp, expansionFraction))
                    .graphicsLayer { alpha = expansionFraction }

                AlbumCarouselSection(
                    currentSong = currentSong,
                    queue = queue,
                    expansionFraction = expansionFraction,
                    onSongSelected = onSongSelectedFromCarousel,
                    modifier = albumArtContainerModifier
                )

                SongMetadataDisplaySection(
                    song = currentSong,
                    expansionFraction = expansionFraction,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    artistTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    onClickLyrics = { /* Lyrics not available in remote mode */ }
                )

                RemotePlaybackControls(
                    modifier = Modifier.padding(top = 16.dp),
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeek = onSeek,
                    onSeekStarted = onSeekStarted,
                    onSeekFinished = onSeekFinished,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thumbColor = MaterialTheme.colorScheme.primary,
                    timeTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                BottomToggleRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp, max = 88.dp)
                        .padding(horizontal = 26.dp, vertical = 8.dp),
                    isShuffleEnabled = isShuffleEnabled,
                    repeatMode = repeatMode,
                    isFavorite = isFavorite,
                    onShuffleToggle = onShuffleToggle,
                    onRepeatToggle = onRepeatToggle,
                    onFavoriteToggle = onFavoriteToggle
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Connecting to device...")
                }
            }
        }
    }
}
package com.theveloper.pixelplay.ui.glancewidget.layouts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.data.model.QueueItem
import com.theveloper.pixelplay.ui.glancewidget.PlayerActions
import com.theveloper.pixelplay.ui.glancewidget.PlayerControlActionCallback
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.EndOfQueuePlaceholder
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PreviousButtonGlance

@Composable
fun ExtraLargeWidgetLayout(
    modifier: GlanceModifier,
    title: String,
    artist: String,
    albumArtBitmapData: ByteArray?,
    isPlaying: Boolean,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    textColor: ColorProvider,
    context: Context,
    queue: List<QueueItem>
) {
    val playButtonCornerRadius = if (isPlaying) 16.dp else 60.dp
    val queueItemWidth = 58.dp
    val queueItemHeight = 52.dp

    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(bgCornerRadius).padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Album Art & Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                AlbumArtImageGlance(
                    bitmapData = albumArtBitmapData,
                    size = 60.dp,
                    context = context,
                    cornerRadius = 16.dp
                )
                Spacer(GlanceModifier.width(16.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = title, style = TextStyle(
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor
                        ), maxLines = 1
                    )
                    Text(
                        text = artist,
                        style = TextStyle(fontSize = 16.sp, color = textColor),
                        maxLines = 1
                    )
                }
            }

            Spacer(GlanceModifier.height(14.dp))

            // Bottom Row: Controls
            Row(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val secondaryColor = GlanceTheme.colors.secondaryContainer
                val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
                val primaryContainerColor = GlanceTheme.colors.primaryContainer
                val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
                val buttonCornerRadius = 60.dp

                PreviousButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    backgroundColor = secondaryColor,
                    iconSize = 28.dp,
                    cornerRadius = buttonCornerRadius
                )
                Spacer(GlanceModifier.width(10.dp))
                PlayPauseButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    isPlaying = isPlaying,
                    iconColor = onPrimaryContainerColor,
                    backgroundColor = primaryContainerColor,
                    iconSize = 30.dp,
                    cornerRadius = playButtonCornerRadius
                )
                Spacer(GlanceModifier.width(10.dp))
                NextButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    backgroundColor = secondaryColor,
                    iconSize = 28.dp,
                    cornerRadius = buttonCornerRadius
                )
            }


            Column {
                Spacer(GlanceModifier.height(12.dp))

                Box(
                    modifier = GlanceModifier.fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 30.dp)
                        .background(textColor.getColor(context).copy(alpha = 0.15f)).height(2.dp)
                        .cornerRadius(60.dp)
                ) {}

                Spacer(GlanceModifier.height(12.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth().height(queueItemHeight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = queue.take(4)
                    val cornerRadius = 14.dp

                    for (i in 0 until 4) {
                        Box(
                            modifier = GlanceModifier.defaultWeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (i < items.size) {
                                val queueItem = items[i]
                                AlbumArtImageGlance(
                                    modifier = GlanceModifier.clickable(
                                        actionRunCallback<PlayerControlActionCallback>(
                                            actionParametersOf(
                                                PlayerActions.key to PlayerActions.PLAY_FROM_QUEUE,
                                                PlayerActions.songIdKey to queueItem.id
                                            )
                                        )
                                    ).width(queueItemWidth).height(queueItemHeight),
                                    bitmapData = queueItem.albumArtBitmapData,
                                    context = context,
                                    cornerRadius = cornerRadius
                                )
                            } else {
                                EndOfQueuePlaceholder(
                                    height = queueItemHeight, width = queueItemWidth, cornerRadius = cornerRadius
                                )
                            }
                        }

                        if (i < 3) {
                            Spacer(GlanceModifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}

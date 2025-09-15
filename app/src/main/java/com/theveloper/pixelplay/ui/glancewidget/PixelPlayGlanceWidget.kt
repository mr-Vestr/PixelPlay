package com.theveloper.pixelplay.ui.glancewidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.theveloper.pixelplay.MainActivity
import com.theveloper.pixelplay.data.model.PlayerInfo
import com.theveloper.pixelplay.ui.glancewidget.layouts.ExtraLargeWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.GabeWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.LargeWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.MediumWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.OneByOneWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.SmallHorizontalWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.SmallWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.ThinWidgetLayout
import com.theveloper.pixelplay.ui.glancewidget.layouts.ThinWidgetLayoutPadded
import com.theveloper.pixelplay.ui.glancewidget.layouts.VeryThinWidgetLayout
import timber.log.Timber

class PixelPlayGlanceWidget : GlanceAppWidget() {

    companion object {
        private val ONE_BY_ONE_SIZE = DpSize(57.dp, 51.dp)
        private val ONE_BY_TWO_SIZE = DpSize(57.dp, 140.dp)
        private val TWO_BY_ONE_SIZE = DpSize(130.dp, 51.dp)
        private val TWO_BY_TWO_SIZE = DpSize(130.dp, 136.dp)
        private val THREE_BY_ONE_SIZE = DpSize(203.dp, 51.dp)
        private val THREE_BY_TWO_SIZE = DpSize(203.dp, 117.dp)
        private val THREE_BY_THREE_SIZE = DpSize(203.dp, 184.dp)
        private val FOUR_BY_TWO_SIZE = DpSize(276.dp, 117.dp)
        private val FOUR_BY_THREE_SIZE = DpSize(276.dp, 184.dp)
        private val FOUR_BY_FOUR_SIZE = DpSize(276.dp, 250.dp)
        private val FIVE_BY_THREE_SIZE = DpSize(349.dp, 216.dp)
        private val FIVE_BY_FOUR_SIZE = DpSize(349.dp, 250.dp)
        private val FIVE_BY_FIVE_SIZE = DpSize(349.dp, 316.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            ONE_BY_ONE_SIZE,
            ONE_BY_TWO_SIZE,
            TWO_BY_ONE_SIZE,
            TWO_BY_TWO_SIZE,
            THREE_BY_ONE_SIZE,
            THREE_BY_TWO_SIZE,
            THREE_BY_THREE_SIZE,
            FOUR_BY_TWO_SIZE,
            FOUR_BY_THREE_SIZE,
            FOUR_BY_FOUR_SIZE,
            FIVE_BY_THREE_SIZE,
            FIVE_BY_FOUR_SIZE,
            FIVE_BY_FIVE_SIZE
        )
    )
    override val stateDefinition = PlayerInfoStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val playerInfo = currentState<PlayerInfo>()
            val currentSize = LocalSize.current

            Timber.tag("PixelPlayGlanceWidget")
                .d("Providing Glance. PlayerInfo: title='${playerInfo.songTitle}', artist='${playerInfo.artistName}', isPlaying=${playerInfo.isPlaying}, hasBitmap=${playerInfo.albumArtBitmapData != null}, progress=${playerInfo.currentPositionMs}/${playerInfo.totalDurationMs}")

            GlanceTheme {
                WidgetUi(playerInfo = playerInfo, size = currentSize, context = context)
            }
        }
    }

    @Composable
    fun WidgetUi(
        playerInfo: PlayerInfo, size: DpSize, context: Context
    ) {
        val title = playerInfo.songTitle.ifEmpty { "PixelPlay" }
        val artist = playerInfo.artistName.ifEmpty { "Toca para abrir" }
        val isPlaying = playerInfo.isPlaying
        val isFavorite = playerInfo.isFavorite
        val albumArtBitmapData = playerInfo.albumArtBitmapData

        Timber.tag("PixelPlayGlanceWidget")
            .d("WidgetUi: PlayerInfo received. Title: $title, Artist: $artist, HasBitmapData: ${albumArtBitmapData != null}, BitmapDataSize: ${albumArtBitmapData?.size ?: "N/A"}")

        val actualBackgroundColor = GlanceTheme.colors.surface
        val onBackgroundColor = GlanceTheme.colors.onSurface

        val baseModifier =
            GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>())

        Box(
            GlanceModifier.fillMaxSize()
        ) {
            when {
                size.width >= FIVE_BY_THREE_SIZE.width && size.height >= FIVE_BY_THREE_SIZE.height -> ExtraLargeWidgetLayout(
                    modifier = baseModifier,
                    title = title,
                    artist = artist,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 28.dp,
                    textColor = onBackgroundColor,
                    context = context,
                    queue = playerInfo.queue
                )

                size.width >= FOUR_BY_THREE_SIZE.width && size.height >= FOUR_BY_THREE_SIZE.height -> LargeWidgetLayout(
                    modifier = baseModifier,
                    title = title,
                    artist = artist,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    isFavorite = isFavorite,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 28.dp,
                    textColor = onBackgroundColor,
                    context = context
                )

                size.width >= FOUR_BY_TWO_SIZE.width && size.height >= FOUR_BY_TWO_SIZE.height -> ThinWidgetLayoutPadded(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 60.dp,
                    title = title,
                    artist = artist,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    textColor = onBackgroundColor,
                    context = context
                )

                size.width >= THREE_BY_THREE_SIZE.width && size.height >= THREE_BY_THREE_SIZE.height -> MediumWidgetLayout(
                    modifier = baseModifier,
                    title = title,
                    artist = artist,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    textColor = onBackgroundColor,
                    context = context,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 28.dp
                )

                size.width >= THREE_BY_TWO_SIZE.width && size.height >= THREE_BY_TWO_SIZE.height -> ThinWidgetLayout(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 28.dp,
                    title = title,
                    artist = artist,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    textColor = onBackgroundColor,
                    context = context
                )

                size.width >= THREE_BY_ONE_SIZE.width && size.height >= THREE_BY_ONE_SIZE.height -> VeryThinWidgetLayout(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 60.dp,
                    title = title,
                    artist = artist,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    textColor = onBackgroundColor,
                    context = context
                )

                size.width >= TWO_BY_TWO_SIZE.width && size.height >= TWO_BY_TWO_SIZE.height -> SmallWidgetLayout(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 28.dp,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    context = context
                )

                size.width >= TWO_BY_ONE_SIZE.width && size.height >= TWO_BY_ONE_SIZE.height -> SmallHorizontalWidgetLayout(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 60.dp,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    context = context
                )

                size.width >= ONE_BY_TWO_SIZE.width && size.width < 59.dp && size.height >= ONE_BY_TWO_SIZE.height -> GabeWidgetLayout(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 360.dp,
                    albumArtBitmapData = albumArtBitmapData,
                    isPlaying = isPlaying,
                    context = context
                )

                else -> OneByOneWidgetLayout(
                    modifier = baseModifier,
                    backgroundColor = actualBackgroundColor,
                    bgCornerRadius = 100.dp,
                    isPlaying = isPlaying
                )
            }
        }
    }
}

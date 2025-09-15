package com.theveloper.pixelplay.ui.glancewidget.layouts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
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
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PreviousButtonGlance
import androidx.glance.layout.Alignment

@Composable
fun SmallWidgetLayout(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    albumArtBitmapData: ByteArray?,
    isPlaying: Boolean,
    context: Context
) {
    val secondaryColor = GlanceTheme.colors.secondaryContainer
    val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
    val primaryContainerColor = GlanceTheme.colors.primaryContainer
    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
    val buttonCornerRadius = 16.dp
    val playButtonCornerRadius = if (isPlaying) 12.dp else 60.dp

    Box(
        modifier = modifier.background(backgroundColor).padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlbumArtImageGlance(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                bitmapData = albumArtBitmapData,
                context = context,
                cornerRadius = 16.dp
            )
            Spacer(GlanceModifier.height(10.dp))
            PlayPauseButtonGlance(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                isPlaying = isPlaying,
                cornerRadius = playButtonCornerRadius,
                iconSize = 26.dp,
                backgroundColor = primaryContainerColor,
                iconColor = onPrimaryContainerColor
            )
            Spacer(GlanceModifier.height(8.dp))
            Row(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreviousButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconSize = 26.dp,
                    cornerRadius = buttonCornerRadius,
                    backgroundColor = secondaryColor,
                    iconColor = onSecondaryColor
                )
                Spacer(GlanceModifier.width(8.dp))
                NextButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconSize = 26.dp,
                    cornerRadius = buttonCornerRadius,
                    backgroundColor = secondaryColor,
                    iconColor = onSecondaryColor
                )
            }
        }
    }
}

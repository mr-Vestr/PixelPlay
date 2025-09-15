package com.theveloper.pixelplay.ui.glancewidget.layouts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance

@Composable
fun SmallHorizontalWidgetLayout(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    albumArtBitmapData: ByteArray?,
    isPlaying: Boolean,
    context: Context
) {
    val primaryContainerColor = GlanceTheme.colors.primaryContainer
    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(bgCornerRadius)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize().cornerRadius(bgCornerRadius),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Spacer(GlanceModifier.width(4.dp))
            AlbumArtImageGlance(
                modifier = GlanceModifier.defaultWeight().padding(vertical = 10.dp),
                bitmapData = albumArtBitmapData,
                context = context,
                cornerRadius = 64.dp
            )
            Spacer(GlanceModifier.width(10.dp))
            PlayPauseButtonGlance(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                backgroundColor = primaryContainerColor,
                iconColor = onPrimaryContainerColor,
                isPlaying = isPlaying,
                iconSize = 26.dp,
                cornerRadius = 10.dp
            )
        }
    }
}

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
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance

@Composable
fun GabeTwoHeightWidgetLayout(
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

    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(bgCornerRadius)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {

            AlbumArtImageGlance(
                modifier = GlanceModifier.defaultWeight().height(48.dp)
                //.padding(4.dp)
                , bitmapData = albumArtBitmapData,
                //size = 48.dp,
                context = context, cornerRadius = 64.dp
            )
            Spacer(GlanceModifier.height(14.dp))
            Column(
                modifier = GlanceModifier.defaultWeight().cornerRadius(bgCornerRadius)
            ) {
                PlayPauseButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    backgroundColor = primaryContainerColor,
                    iconColor = onPrimaryContainerColor,
                    isPlaying = isPlaying,
                    iconSize = 26.dp,
                    cornerRadius = 10.dp
                )
                Spacer(GlanceModifier.height(10.dp))
                NextButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    iconColor = onSecondaryColor,
                    iconSize = 26.dp,
                    backgroundColor = secondaryColor,
                    cornerRadius = 10.dp
                )
            }
        }
    }
}

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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PreviousButtonGlance

@Composable
fun GabeWidgetLayout(
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
        modifier = modifier // Apply the base modifier for click handling and sizing
            .background(backgroundColor).cornerRadius(bgCornerRadius).padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.padding(10.dp).fillMaxSize()
                .cornerRadius(bgCornerRadius), // Padding applied to the content area
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize().cornerRadius(bgCornerRadius),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = GlanceModifier.fillMaxWidth().cornerRadius(360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumArtImageGlance(
                        modifier = GlanceModifier.size(60.dp),
                        bitmapData = albumArtBitmapData,
                        context = context,
                        cornerRadius = 360.dp
                    )
                }

                Spacer(GlanceModifier.height(8.dp))


                Column(
                    modifier = GlanceModifier.defaultWeight().cornerRadius(bgCornerRadius - 10.dp)
                ) {
                    PreviousButtonGlance(
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                        iconColor = onSecondaryColor,
                        iconSize = 26.dp,
                        backgroundColor = secondaryColor,
                        cornerRadius = 10.dp
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    PlayPauseButtonGlance(
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                        backgroundColor = primaryContainerColor,
                        iconColor = onPrimaryContainerColor,
                        isPlaying = isPlaying,
                        iconSize = 26.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(GlanceModifier.height(4.dp))
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
}

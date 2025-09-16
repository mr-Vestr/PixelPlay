package com.theveloper.pixelplay.ui.glancewidget.layouts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance

@Composable
fun ThinWidgetLayout(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    title: String,
    artist: String,
    albumArtBitmapData: ByteArray?,
    isPlaying: Boolean,
    textColor: ColorProvider,
    context: Context
) {
    val secondaryColor = GlanceTheme.colors.secondaryContainer
    val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
    val primaryContainerColor = GlanceTheme.colors.primaryContainer
    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(bgCornerRadius)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title, style = TextStyle(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor
                ), maxLines = 1, modifier = GlanceModifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AlbumArtImageGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    bitmapData = albumArtBitmapData,
                    context = context,
                    cornerRadius = bgCornerRadius
                )

                Spacer(GlanceModifier.width(8.dp))

                PlayPauseButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    backgroundColor = primaryContainerColor,
                    iconColor = onPrimaryContainerColor,
                    isPlaying = isPlaying,
                    iconSize = 26.dp,
                    cornerRadius = bgCornerRadius
                )
                Spacer(GlanceModifier.width(8.dp))
                NextButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    iconSize = 26.dp,
                    backgroundColor = secondaryColor,
                    cornerRadius = bgCornerRadius
                )
            }
        }
    }
}

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
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PreviousButtonGlance
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box

@Composable
fun MediumWidgetLayout(
    modifier: GlanceModifier,
    title: String,
    artist: String,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    albumArtBitmapData: ByteArray?,
    isPlaying: Boolean,
    textColor: ColorProvider,
    context: Context
) {
    val secondaryColor = GlanceTheme.colors.secondaryContainer
    val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
    val primaryContainerColor = GlanceTheme.colors.primaryContainer
    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
    val buttonCornerRadius = 60.dp
    val playButtonCornerRadius = if (isPlaying) 14.dp else 60.dp

    // *** FIX: Apply padding to the outer Box for consistency ***
    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(bgCornerRadius)
            .padding(16.dp)
    ) {
        // *** FIX: Removed padding from the inner Column ***
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top part: Album Art + Title/Artist
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImageGlance(
                    bitmapData = albumArtBitmapData,
                    size = 80.dp,
                    context = context,
                    cornerRadius = 16.dp
                )
                Spacer(GlanceModifier.width(12.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = title, style = TextStyle(
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor
                        ), maxLines = 2
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = artist,
                        style = TextStyle(fontSize = 13.sp, color = textColor),
                        maxLines = 2
                    )
                }
            }

            // Spacer to push buttons down
            Spacer(GlanceModifier.height(12.dp))

            // Bottom part: Control Buttons
            Row(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviousButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    backgroundColor = secondaryColor,
                    cornerRadius = buttonCornerRadius
                )
                Spacer(GlanceModifier.width(8.dp))
                PlayPauseButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    isPlaying = isPlaying,
                    iconColor = onPrimaryContainerColor,
                    backgroundColor = primaryContainerColor,
                    cornerRadius = playButtonCornerRadius
                )
                Spacer(GlanceModifier.width(8.dp))
                NextButtonGlance(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    iconColor = onSecondaryColor,
                    backgroundColor = secondaryColor,
                    cornerRadius = buttonCornerRadius
                )
            }
        }
    }
}

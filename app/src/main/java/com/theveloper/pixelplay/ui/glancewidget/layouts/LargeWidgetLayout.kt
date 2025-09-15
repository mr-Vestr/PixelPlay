package com.theveloper.pixelplay.ui.glancewidget.layouts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.ui.glancewidget.PlayerActions
import com.theveloper.pixelplay.ui.glancewidget.PlayerControlActionCallback
import com.theveloper.pixelplay.ui.glancewidget.components.AlbumArtImageGlance
import com.theveloper.pixelplay.ui.glancewidget.components.NextButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance
import com.theveloper.pixelplay.ui.glancewidget.components.PreviousButtonGlance

@Composable
fun LargeWidgetLayout(
    modifier: GlanceModifier,
    title: String,
    artist: String,
    albumArtBitmapData: ByteArray?,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    isPlaying: Boolean,
    isFavorite: Boolean,
    textColor: ColorProvider,
    context: Context
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                AlbumArtImageGlance(
                    bitmapData = albumArtBitmapData,
                    size = 64.dp,
                    context = context,
                    cornerRadius = 18.dp
                )
                Spacer(GlanceModifier.width(12.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = title, style = TextStyle(
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor
                        ), maxLines = 1
                    )
                    Text(
                        text = artist,
                        style = TextStyle(fontSize = 13.sp, color = textColor),
                        maxLines = 1
                    )
                }
                Spacer(GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(if (isFavorite) R.drawable.round_favorite_24 else R.drawable.rounded_favorite_24),
                    contentDescription = "favorite",
                    modifier = GlanceModifier.size(28.dp).clickable(
                        actionRunCallback<PlayerControlActionCallback>(
                            actionParametersOf(PlayerActions.key to PlayerActions.FAVORITE)
                        )
                    ).padding(2.dp),
                    colorFilter = ColorFilter.tint(textColor)
                )
                Spacer(GlanceModifier.width(8.dp))
            }
            Spacer(GlanceModifier.height(4.dp))
            // Progress bar commented out as in original code
            Spacer(GlanceModifier.height(10.dp))

            // Control Buttons Row
            Row(
                modifier = GlanceModifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val secondaryColor = GlanceTheme.colors.secondaryContainer
                val onSecondaryColor = GlanceTheme.colors.onSecondaryContainer
                val primaryContainerColor = GlanceTheme.colors.primaryContainer
                val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer
                val buttonCornerRadius = 60.dp
                val playButtonCornerRadius = if (isPlaying) 14.dp else 60.dp

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

package com.theveloper.pixelplay.ui.glancewidget.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.ui.glancewidget.components.PlayPauseButtonGlance

@Composable
fun OneByOneWidgetLayout(
    modifier: GlanceModifier,
    backgroundColor: ColorProvider,
    bgCornerRadius: Dp,
    isPlaying: Boolean
) {
    val primaryContainerColor = GlanceTheme.colors.primaryContainer
    val onPrimaryContainerColor = GlanceTheme.colors.onPrimaryContainer

    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(bgCornerRadius)
            .padding(10.dp), contentAlignment = Alignment.Center
    ) {
        PlayPauseButtonGlance(
            modifier = GlanceModifier.fillMaxSize(),
            backgroundColor = primaryContainerColor,
            iconColor = onPrimaryContainerColor,
            isPlaying = isPlaying,
            iconSize = 36.dp,
            cornerRadius = bgCornerRadius
        )
    }
}

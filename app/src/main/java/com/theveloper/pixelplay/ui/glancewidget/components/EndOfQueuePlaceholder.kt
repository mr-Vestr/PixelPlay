package com.theveloper.pixelplay.ui.glancewidget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.size

@Composable
fun EndOfQueuePlaceholder(
    modifier: GlanceModifier = GlanceModifier, width: Dp, height: Dp, cornerRadius: Dp
) {
    Box(
        modifier = modifier.size(width, height).background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(cornerRadius)
    ) {

    }
}

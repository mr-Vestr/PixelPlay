package com.theveloper.pixelplay.ui.glancewidget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.ui.glancewidget.PlayerActions
import com.theveloper.pixelplay.ui.glancewidget.PlayerControlActionCallback

@Composable
fun NextButtonGlance(
    modifier: GlanceModifier = GlanceModifier,
    iconColor: ColorProvider = GlanceTheme.colors.onSurfaceVariant,
    backgroundColor: ColorProvider = GlanceTheme.colors.surfaceVariant,
    iconSize: Dp = 24.dp,
    cornerRadius: Dp = 8.dp
) {
    val params = actionParametersOf(PlayerActions.key to PlayerActions.NEXT)
    Box(
        modifier = modifier.background(backgroundColor).cornerRadius(cornerRadius)
            .clickable(actionRunCallback<PlayerControlActionCallback>(params)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(R.drawable.rounded_skip_next_24),
            contentDescription = "Next",
            modifier = GlanceModifier.size(iconSize),
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
}

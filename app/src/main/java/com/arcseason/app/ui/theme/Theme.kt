package com.arcseason.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Arc Season is a dark-theme-only product by design spec (Material3 dark,
// #121212 / #00E676). We still read isSystemInDarkTheme() so a future light
// variant is a one-line addition rather than a rewrite.
private val ArcDarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = Color.Black,
    primaryContainer = NeonEmeraldContainer,
    onPrimaryContainer = NeonEmerald,
    secondary = NeonEmerald,
    onSecondary = Color.Black,
    background = ArcBackground,
    onBackground = ArcOnBackground,
    surface = ArcSurface,
    onSurface = ArcOnBackground,
    surfaceVariant = ArcSurfaceVariant,
    onSurfaceVariant = ArcOnSurfaceMuted,
    error = ArcError,
    onError = Color.Black,
    outline = ArcDivider
)

@Composable
fun ArcSeasonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcDarkColorScheme,
        typography = ArcTypography,
        content = content
    )
}

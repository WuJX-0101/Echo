package com.echo.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = WarmBrown,
    onPrimary = Cream,
    primaryContainer = SoftPink,
    onPrimaryContainer = TextPrimary,
    secondary = WarmBrown,
    onSecondary = Cream,
    secondaryContainer = Cream,
    onSecondaryContainer = TextPrimary,
    background = WarmBeige,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondary,
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmBrownDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = SoftPinkDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = WarmBrownDark,
    onSecondary = TextPrimaryDark,
    secondaryContainer = CreamDark,
    onSecondaryContainer = TextPrimaryDark,
    background = WarmBeigeDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceLightDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardLightDark,
    onSurfaceVariant = TextSecondaryDark,
)

@Composable
fun EchoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.gymflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GymGreen,
    onPrimary = BackgroundDark,
    primaryContainer = GymGreenDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentOrange,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceLight,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentBlue,
    onTertiary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = FormError,
    onError = TextPrimary,
    outline = DividerColor
)

private val LightColorScheme = lightColorScheme(
    primary = GymGreenDark,
    onPrimary = TextPrimary,
    primaryContainer = GymGreenLight,
    onPrimaryContainer = BackgroundDark,
    secondary = AccentOrange,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    error = FormError,
    onError = TextPrimary
)

@Composable
fun GymFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
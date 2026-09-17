package com.palette.mobile.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WarmTextPrimaryDark,
    background = WarmBackgroundDark,
    surface = WarmSurfaceDark,
    onPrimary = WarmBackgroundDark,
    onBackground = WarmTextPrimaryDark,
    onSurface = WarmTextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = WarmTextPrimaryLight,
    background = WarmBackgroundLight,
    surface = WarmSurfaceLight,
    onPrimary = WarmSurfaceLight,
    onBackground = WarmTextPrimaryLight,
    onSurface = WarmTextPrimaryLight
)

@Composable
fun PaletteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

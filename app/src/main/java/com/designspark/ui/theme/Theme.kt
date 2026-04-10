package com.designspark.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = AccentCoral,
    onPrimary = Color.White,
    primaryContainer = AccentCoralSoft,
    onPrimaryContainer = InkPrimary,
    secondary = InkSecondary,
    onSecondary = Color.White,
    tertiary = InkTertiary,
    onTertiary = Color.White,
    background = CanvasLight,
    onBackground = InkPrimary,
    surface = SurfaceLight,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = InkSecondary,
    outline = BorderSubtleLight,
    outlineVariant = BorderFocus,
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkCanvas,
    primaryContainer = Color(0xFF3D2E28),
    onPrimaryContainer = DarkInkPrimary,
    secondary = DarkInkSecondary,
    onSecondary = DarkCanvas,
    tertiary = DarkInkSecondary,
    onTertiary = DarkCanvas,
    background = DarkCanvas,
    onBackground = DarkInkPrimary,
    surface = DarkSurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = Color(0xFF2E2C28),
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF4A4640),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Claude UI is light-first; keep false to preserve warm paper aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> LightScheme
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.computerization.outspire.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightBg = Color(0xFFF7F7FA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF0F1F6)
private val LightOnSurfaceVariant = Color(0xFF2C2F3A)
private val LightOutline = Color(0xFFD0D3DD)

private val LightColors = lightColorScheme(
    primary = BrandTint,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
)

private val DarkColors = darkColorScheme(
    primary = BrandTintDark,
    background = RichDarkBg,
    surface = RichDarkSurface,
    surfaceVariant = RichDarkSurfaceElevated,
)

@Composable
fun OutspireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = OutspireTypography,
        content = content,
    )
}

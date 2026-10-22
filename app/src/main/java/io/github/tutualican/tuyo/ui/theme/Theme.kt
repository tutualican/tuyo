package io.github.tutualican.tuyo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalTuyoColors = staticCompositionLocalOf { DarkColors }

@Composable
fun TuyoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            primaryContainer = colors.accentSoft,
            onPrimaryContainer = colors.textPrimary,
            background = colors.canvas,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.raised,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.hairlineStrong,
            outlineVariant = colors.hairline,
            error = colors.danger,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            primaryContainer = colors.accentSoft,
            onPrimaryContainer = colors.textPrimary,
            background = colors.canvas,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.raised,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.hairlineStrong,
            outlineVariant = colors.hairline,
            error = colors.danger,
        )
    }

    CompositionLocalProvider(LocalTuyoColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = TuyoTypography,
            shapes = TuyoShapes,
            content = content,
        )
    }
}

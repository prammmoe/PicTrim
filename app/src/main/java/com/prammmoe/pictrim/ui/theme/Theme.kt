package com.prammmoe.pictrim.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable fun PicTrimTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val scheme = if (darkTheme) darkColorScheme(
        primary = DarkInk, onPrimary = DarkCanvas, secondary = DarkSurfaceMuted,
        onSecondary = DarkInk, background = DarkCanvas, onBackground = DarkInk,
        surface = DarkSurface, onSurface = DarkInk, surfaceVariant = DarkSurfaceMuted,
        onSurfaceVariant = DarkInkSoft, outline = DarkLine, error = Error
    ) else lightColorScheme(
        primary = Ink, onPrimary = Surface, secondary = SurfaceMuted,
        onSecondary = Ink, background = Canvas, onBackground = Ink,
        surface = Surface, onSurface = Ink, surfaceVariant = SurfaceMuted,
        onSurfaceVariant = InkSoft, outline = Line, error = Error
    )
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect { WindowCompat.getInsetsController((view.context as Activity).window, view).isAppearanceLightStatusBars = !darkTheme }
    MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
}

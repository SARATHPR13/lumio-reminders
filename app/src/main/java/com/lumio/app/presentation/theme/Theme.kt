package com.lumio.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { LIGHT, DARK, AMOLED, SYSTEM }

val LocalAmoledMode = compositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary            = LumioBlue,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary          = LumioPurple,
    onSecondary        = Color.White,
    background         = BackgroundLight,
    onBackground       = TextPrimaryLight,
    surface            = SurfaceLight,
    onSurface          = TextPrimaryLight,
    surfaceVariant     = SurfaceVarLight,
    onSurfaceVariant   = TextSecondaryLight,
    error              = ColorError,
    onError            = Color.White,
)

private val DarkColors = darkColorScheme(
    primary            = LumioBlueDim,
    onPrimary          = Color(0xFF003064),
    primaryContainer   = Color(0xFF004594),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary          = LumioPurpleDim,
    onSecondary        = Color(0xFF4B007F),
    background         = BackgroundDark,
    onBackground       = TextPrimaryDark,
    surface            = SurfaceDark,
    onSurface          = TextPrimaryDark,
    surfaceVariant     = SurfaceVarDark,
    onSurfaceVariant   = TextSecondaryDark,
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005),
)

private val AmoledColors = DarkColors.copy(
    background     = BackgroundAMOLED,
    surface        = SurfaceAMOLED,
    surfaceVariant = SurfaceVarAMOLED,
)

@Composable
fun LumioTheme(
    themeMode: ThemeMode  = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT          -> false
        ThemeMode.DARK,
        ThemeMode.AMOLED         -> true
        ThemeMode.SYSTEM         -> isSystemInDarkTheme()
    }
    val isAmoled = themeMode == ThemeMode.AMOLED
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            when {
                isAmoled -> dynamicDarkColorScheme(ctx).copy(
                    background     = BackgroundAMOLED,
                    surface        = SurfaceAMOLED,
                    surfaceVariant = SurfaceVarAMOLED,
                )
                isDark   -> dynamicDarkColorScheme(ctx)
                else     -> dynamicLightColorScheme(ctx)
            }
        }
        isAmoled -> AmoledColors
        isDark   -> DarkColors
        else     -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }
    CompositionLocalProvider(LocalAmoledMode provides isAmoled) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = LumioTypography,
            content     = content
        )
    }
}

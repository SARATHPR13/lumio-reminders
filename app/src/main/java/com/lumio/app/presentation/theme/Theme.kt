package com.lumio.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Clear Morning light scheme (the redesign) ──
private val LightColorScheme = lightColorScheme(
    primary = LumioAccent,
    onPrimary = Color.White,
    primaryContainer = LumioAccentSoft,
    onPrimaryContainer = LumioAccentInk,
    secondary = LumioSlate,
    onSecondary = Color.White,
    secondaryContainer = LumioSlateSoft,
    onSecondaryContainer = Color(0xFF2E3A32),
    tertiary = LumioAccent,
    onTertiary = Color.White,
    tertiaryContainer = ClearSurfaceSoft,
    onTertiaryContainer = LumioAccentInk,
    background = ClearCanvas,
    onBackground = ClearInk,
    surface = ClearSurface,
    onSurface = ClearInk,
    surfaceVariant = ClearSurfaceSoft,
    onSurfaceVariant = ClearMuted,
    outline = ClearLine,
    outlineVariant = ClearLineSoft,
    error = CalmError,
    onError = Color.White,
    errorContainer = CalmErrorSoft,
    onErrorContainer = CalmErrorInk,
    inverseSurface = Color(0xFF2A2E2B),
    inverseOnSurface = ClearCanvas,
    scrim = Color(0xFF000000)
)

// ── Dark scheme (unchanged for now; light is the focus of the redesign) ──
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7FC0A0),
    onPrimary = Color(0xFF10281C),
    primaryContainer = Color(0xFF2C5D42),
    onPrimaryContainer = Color(0xFFCDEBD9),
    secondary = Color(0xFFA9C2B3),
    onSecondary = Color(0xFF20362A),
    secondaryContainer = Color(0xFF394E42),
    onSecondaryContainer = Color(0xFFD6E6DC),
    tertiary = Color(0xFF7FC0A0),
    onTertiary = Color(0xFF10281C),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkOnSurface2,
    outline = DarkDivider,
    outlineVariant = DarkSurface3,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    inverseSurface = ClearSurface,
    inverseOnSurface = ClearInk,
    scrim = Color(0xFF000000)
)

private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF7FC0A0),
    onPrimary = Color(0xFF10281C),
    primaryContainer = Color(0xFF1E3A2B),
    onPrimaryContainer = Color(0xFFCDEBD9),
    secondary = Color(0xFFA9C2B3),
    onSecondary = Color(0xFF20362A),
    secondaryContainer = Color(0xFF243029),
    onSecondaryContainer = Color(0xFFD6E6DC),
    tertiary = Color(0xFF7FC0A0),
    onTertiary = Color(0xFF10281C),
    background = AmoledBackground,
    onBackground = DarkOnSurface,
    surface = AmoledSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = AmoledSurface2,
    onSurfaceVariant = DarkOnSurface2,
    outline = Color(0xFF1E293B),
    outlineVariant = Color(0xFF0F172A),
    error = Color(0xFFF2B8B5),
    onError = Color.White,
    errorContainer = Color(0xFF1A0000),
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun LumioTheme(
    darkTheme : Boolean = isSystemInDarkTheme(),
    amoledTheme : Boolean = false,
    // Redesign: default OFF so the Clear Morning brand palette actually shows.
    // (When true on Android 12+, colors came from the phone wallpaper instead.)
    dynamicColor: Boolean = false,
    content : @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            when {
                amoledTheme -> AmoledColorScheme
                darkTheme -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
        }
        amoledTheme -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme && !amoledTheme
                isAppearanceLightNavigationBars = !darkTheme && !amoledTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumioTypography,
        content = content
    )
}

object ThemeMode {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val AMOLED = "amoled"
    const val SYSTEM = "system"
}

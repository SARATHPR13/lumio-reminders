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

private val LightColorScheme = lightColorScheme(
    primary            = LumioBlue,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFDCEAFD),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary          = LumioPurple,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = LumioPurpleDark,
    tertiary           = LumioTeal,
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFFCCFBF1),
    background         = LightBackground,
    onBackground       = LightOnSurface,
    surface            = LightSurface,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightSurface2,
    onSurfaceVariant   = LightOnSurface2,
    outline            = LightDivider,
    outlineVariant     = LightSurface3,
    error              = LumioRed,
    onError            = Color.White,
    errorContainer     = Color(0xFFFEE2E2),
    onErrorContainer   = Color(0xFF7F1D1D),
    inverseSurface     = Color(0xFF1E293B),
    inverseOnSurface   = Color(0xFFF8FAFC),
    scrim              = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary            = LumioBlueLight,
    onPrimary          = Color(0xFF1E3A8A),
    primaryContainer   = LumioBlueDark,
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary          = Color(0xFFA78BFA),
    onSecondary        = Color(0xFF4C1D95),
    secondaryContainer = LumioPurpleDark,
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary           = Color(0xFF2DD4BF),
    onTertiary         = Color(0xFF042F2E),
    tertiaryContainer  = LumioTeal,
    background         = DarkBackground,
    onBackground       = DarkOnSurface,
    surface            = DarkSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = DarkOnSurface2,
    outline            = DarkDivider,
    outlineVariant     = DarkSurface3,
    error              = Color(0xFFF87171),
    onError            = Color(0xFF7F1D1D),
    errorContainer     = Color(0xFF450A0A),
    onErrorContainer   = Color(0xFFFECACA),
    inverseSurface     = LightSurface,
    inverseOnSurface   = LightOnSurface,
    scrim              = Color(0xFF000000)
)

private val AmoledColorScheme = darkColorScheme(
    primary            = LumioBlueLight,
    onPrimary          = Color(0xFF1E3A8A),
    primaryContainer   = Color(0xFF1D3461),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary          = Color(0xFFA78BFA),
    onSecondary        = Color(0xFF4C1D95),
    secondaryContainer = Color(0xFF2D1B69),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary           = Color(0xFF2DD4BF),
    onTertiary         = Color(0xFF042F2E),
    background         = AmoledBackground,
    onBackground       = DarkOnSurface,
    surface            = AmoledSurface,
    onSurface          = DarkOnSurface,
    surfaceVariant     = AmoledSurface2,
    onSurfaceVariant   = DarkOnSurface2,
    outline            = Color(0xFF1E293B),
    outlineVariant     = Color(0xFF0F172A),
    error              = Color(0xFFF87171),
    onError            = Color.White,
    errorContainer     = Color(0xFF1A0000),
    onErrorContainer   = Color(0xFFFECACA)
)

@Composable
fun LumioTheme(
    darkTheme   : Boolean = isSystemInDarkTheme(),
    amoledTheme : Boolean = false,
    dynamicColor: Boolean = true,
    content     : @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            when {
                amoledTheme -> AmoledColorScheme
                darkTheme   -> dynamicDarkColorScheme(context)
                else        -> dynamicLightColorScheme(context)
            }
        }
        amoledTheme -> AmoledColorScheme
        darkTheme   -> DarkColorScheme
        else        -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !darkTheme && !amoledTheme
                isAppearanceLightNavigationBars = !darkTheme && !amoledTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = LumioTypography,
        content     = content
    )
}

#!/data/data/com.termux/files/usr/bin/bash
# LUMIO redesign — Step 1: theme foundation (Clear Morning palette)
# Run from the ROOT of the lumio-reminders repo.
set -e

if [ ! -d "app/src/main/java/com/lumio/app" ]; then
  echo "ERROR: run this from the root of the lumio-reminders repo (the folder that contains 'app/')."
  exit 1
fi

SRC="app/src/main/java/com/lumio/app"
THEME="$SRC/presentation/theme"

echo "1/3  Writing Color.kt ..."
cat > "$THEME/Color.kt" << 'EOF'
package com.lumio.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ── Brand Colors (legacy — kept so existing screens still compile) ──
val LumioBlue = Color(0xFF2563EB)
val LumioBlueDark = Color(0xFF1D4ED8)
val LumioBlueLight = Color(0xFF60A5FA)
val LumioPurple = Color(0xFF7C3AED)
val LumioPurpleDark = Color(0xFF6D28D9)
val LumioTeal = Color(0xFF0D9488)
val LumioGreen = Color(0xFF059669)
val LumioOrange = Color(0xFFEA580C)
val LumioRed = Color(0xFFDC2626)
val LumioAmber = Color(0xFFD97706)

// ── Legacy light/dark/amoled neutrals (kept for compatibility) ──
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFF1F5F9)
val LightSurface3 = Color(0xFFE2E8F0)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurface2 = Color(0xFF475569)
val LightOnSurface3 = Color(0xFF94A3B8)
val LightDivider = Color(0xFFE2E8F0)

val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurface2 = Color(0xFF334155)
val DarkSurface3 = Color(0xFF475569)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurface2 = Color(0xFFCBD5E1)
val DarkOnSurface3 = Color(0xFF94A3B8)
val DarkDivider = Color(0xFF334155)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0A)
val AmoledSurface2 = Color(0xFF141414)

val GradientBlue = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
val GradientGreen = listOf(Color(0xFF059669), Color(0xFF0D9488))
val GradientOrange = listOf(Color(0xFFEA580C), Color(0xFFD97706))
val GradientPurple = listOf(Color(0xFF7C3AED), Color(0xFFDB2777))
val GradientRed = listOf(Color(0xFFDC2626), Color(0xFFEA580C))

// ══════════════════════════════════════════════════════════════
//  CLEAR MORNING — the redesign palette (calm, luminous, minimal)
// ══════════════════════════════════════════════════════════════
val LumioAccent      = Color(0xFF3B7A57) // sage / forest — primary action
val LumioAccentSoft  = Color(0xFFE8F1EB) // light green tint — selected states
val LumioAccentInk   = Color(0xFF2C5D42) // deep green — text on soft green

val ClearCanvas      = Color(0xFFF4F5F3) // warm off-white — app background
val ClearSurface     = Color(0xFFFFFFFF) // cards lift with pure white
val ClearSurfaceSoft = Color(0xFFEDEFEC) // faint neutral fill
val ClearInk         = Color(0xFF1B1D1C) // warm near-black — primary text
val ClearMuted       = Color(0xFF7A7E7B) // secondary text
val ClearFaint       = Color(0xFFAFB2AF) // tertiary / disabled
val ClearLine        = Color(0xFFEAEAE6) // hairline dividers / borders
val ClearLineSoft    = Color(0xFFDEE0DC) // slightly stronger hairline

val LumioSlate       = Color(0xFF566B60) // muted green-grey — secondary
val LumioSlateSoft   = Color(0xFFE4EAE5)

val CalmError        = Color(0xFFC0483B)
val CalmErrorSoft    = Color(0xFFF7E4E1)
val CalmErrorInk     = Color(0xFF7A2318)
EOF

echo "2/3  Writing Theme.kt ..."
cat > "$THEME/Theme.kt" << 'EOF'
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
EOF

echo "3/3  Writing MainActivity.kt ..."
cat > "$SRC/MainActivity.kt" << 'EOF'
package com.lumio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lumio.app.crash.CrashHandler
import com.lumio.app.data.preferences.AppPreferences
import com.lumio.app.presentation.navigation.LumioNavGraph
import com.lumio.app.presentation.screens.crash.CrashReportScreen
import com.lumio.app.presentation.screens.splash.SplashScreen
import com.lumio.app.presentation.theme.LumioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val pendingCrash = CrashHandler.getLastCrash(this)

        setContent {
            val themeMode by appPreferences.themeMode.collectAsState(initial = "light")

            LumioTheme(
                darkTheme = when (themeMode) {
                    "dark", "amoled" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                },
                amoledTheme = themeMode == "amoled",
                // Redesign: use the Clear Morning brand palette, not wallpaper colors.
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var crashTrace by remember { mutableStateOf(pendingCrash) }
                    var showSplash by remember { mutableStateOf(crashTrace == null) }

                    LaunchedEffect(Unit) {
                        if (crashTrace == null) {
                            delay(900)
                            showSplash = false
                        }
                    }

                    when {
                        crashTrace != null -> CrashReportScreen(
                            trace = crashTrace ?: "",
                            onContinue = { crashTrace = null }
                        )
                        showSplash -> SplashScreen()
                        else -> {
                            val navController = rememberNavController()
                            LumioNavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
EOF

echo ""
echo "Done. Three files updated. Next:"
echo "  git add -A"
echo "  git commit -m \"Redesign step 1: Clear Morning theme foundation\""
echo "  git push"

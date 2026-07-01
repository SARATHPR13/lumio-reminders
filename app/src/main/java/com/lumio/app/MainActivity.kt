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

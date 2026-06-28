package com.lumio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lumio.app.data.preferences.AppPreferences
import com.lumio.app.presentation.navigation.LumioNavGraph
import com.lumio.app.presentation.permission.LumioPermissions
import com.lumio.app.presentation.theme.LumioTheme
import com.lumio.app.presentation.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode    by appPreferences.themeMode.collectAsState(ThemeMode.SYSTEM)
            val dynamicColor by appPreferences.dynamicColor.collectAsState(true)

            LumioTheme(
                themeMode    = themeMode,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    LumioPermissions {
                        LumioNavGraph()
                    }
                }
            }
        }
    }
}

package com.lumio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lumio.app.data.preferences.AppPreferences
import com.lumio.app.presentation.navigation.LumioNavGraph
import com.lumio.app.presentation.theme.LumioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by appPreferences.themeMode.collectAsState(initial = "system")

            LumioTheme(
                darkTheme    = when (themeMode) {
                    "dark", "amoled" -> true
                    "light"          -> false
                    else             -> isSystemInDarkTheme()
                },
                amoledTheme  = themeMode == "amoled",
                dynamicColor = true
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    LumioNavGraph(navController = navController)
                }
            }
        }
    }
}

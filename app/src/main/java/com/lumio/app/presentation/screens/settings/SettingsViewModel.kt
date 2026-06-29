package com.lumio.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode        : String  = "system",
    val dynamicColors    : Boolean = true,
    val defaultSound     : Boolean = true,
    val defaultVibration : Boolean = true,
    val biometricEnabled : Boolean = false,
    val language         : String  = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { loadSettings() }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                prefs.themeMode,
                prefs.dynamicColors,
                prefs.defaultSound,
                prefs.defaultVibration,
                prefs.language
            ) { values ->
                SettingsUiState(
                    themeMode        = values[0] as String,
                    dynamicColors    = values[1] as Boolean,
                    defaultSound     = values[2] as Boolean,
                    defaultVibration = values[3] as Boolean,
                    language         = values[4] as String
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColors(v: Boolean) {
        viewModelScope.launch { prefs.setDynamicColors(v) }
    }

    fun setDefaultSound(v: Boolean) {
        viewModelScope.launch { prefs.setDefaultSound(v) }
    }

    fun setDefaultVibration(v: Boolean) {
        viewModelScope.launch { prefs.setDefaultVibration(v) }
    }

    fun setBiometricEnabled(v: Boolean) {
        _uiState.update { it.copy(biometricEnabled = v) }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { prefs.setLanguage(code) }
        _uiState.update { it.copy(language = code) }
    }
}

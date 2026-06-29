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
    val fontSize         : String  = "medium",
    val successMessage   : String? = null
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
                prefs.defaultVibration
            ) { theme, dynamic, sound, vibration ->
                SettingsUiState(
                    themeMode        = theme,
                    dynamicColors    = dynamic,
                    defaultSound     = sound,
                    defaultVibration = vibration
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColors(enabled) }
    }

    fun setDefaultSound(enabled: Boolean) {
        viewModelScope.launch { prefs.setDefaultSound(enabled) }
    }

    fun setDefaultVibration(enabled: Boolean) {
        viewModelScope.launch { prefs.setDefaultVibration(enabled) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _uiState.update { it.copy(biometricEnabled = enabled) }
    }

    fun backupData() {
        _uiState.update { it.copy(successMessage = "Backup coming soon!") }
    }

    fun restoreData() {
        _uiState.update { it.copy(successMessage = "Restore coming soon!") }
    }

    fun clearAllReminders() {
        _uiState.update { it.copy(successMessage = "Clear coming soon!") }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

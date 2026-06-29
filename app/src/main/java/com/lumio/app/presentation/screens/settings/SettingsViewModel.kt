package com.lumio.app.presentation.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.data.preferences.AppPreferences
import com.lumio.app.data.preferences.FontSize
import com.lumio.app.data.repository.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode        = "system",
    val dynamicColor: Boolean       = true,
    val fontSize: FontSize          = FontSize.MEDIUM,
    val defaultSound: Boolean       = true,
    val defaultVibration: Boolean   = true,
    val snoozeDefault: Int          = 5,
    val pinEnabled: Boolean         = false,
    val biometricEnabled: Boolean   = false,
    val showHidden: Boolean         = false,
    val autoBackup: Boolean         = false,
    val lastBackupText: String      = "Never",
    val language: String            = "English",
    val isLoading: Boolean          = false,
    val successMessage: String?     = null,
    val errorMessage: String?       = null,
    val showPinSetup: Boolean       = false,
    val showPinInput: String        = "",
    val showPinConfirm: String      = "",
    val pinStep: PinStep            = PinStep.ENTER,
    val backupFiles: List<File>     = emptyList(),
    val showBackupDialog: Boolean   = false,
    val showRestoreDialog: Boolean  = false
)

enum class PinStep { ENTER, CONFIRM }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
    private val backupService: BackupService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { observePreferences() }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                prefs.themeMode,
                prefs.dynamicColor,
                prefs.fontSize,
                prefs.defaultSound,
                prefs.defaultVibration
            ) { theme, dynamic, font, sound, vibration ->
                _uiState.update {
                    it.copy(
                        themeMode      = theme,
                        dynamicColor   = dynamic,
                        fontSize       = font,
                        defaultSound   = sound,
                        defaultVibration = vibration
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            combine(
                prefs.pinEnabled,
                prefs.biometricEnabled,
                prefs.showHidden,
                prefs.autoBackup,
                prefs.lastBackup
            ) { pin, bio, hidden, autoBk, lastBk ->
                _uiState.update {
                    it.copy(
                        pinEnabled       = pin,
                        biometricEnabled = bio,
                        showHidden       = hidden,
                        autoBackup       = autoBk,
                        lastBackupText   = if (lastBk == 0L) "Never"
                        else SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                            .format(Date(lastBk))
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            combine(prefs.language, prefs.snoozeDefault) { lang, snooze ->
                _uiState.update { it.copy(language = lang, snoozeDefault = snooze) }
            }.collect()
        }
    }

    // ── Theme ─────────────────────────────────────────
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    // ── Font ──────────────────────────────────────────
    fun setFontSize(size: FontSize) {
        viewModelScope.launch { prefs.setFontSize(size) }
    }

    // ── Notifications ────────────────────────────────
    fun setDefaultSound(enabled: Boolean) {
        viewModelScope.launch { prefs.setDefaultSound(enabled) }
    }

    fun setDefaultVibration(enabled: Boolean) {
        viewModelScope.launch { prefs.setDefaultVibration(enabled) }
    }

    fun setSnoozeDefault(minutes: Int) {
        viewModelScope.launch { prefs.setSnoozeDefault(minutes) }
    }

    // ── Security ──────────────────────────────────────
    fun showPinSetup(show: Boolean) {
        _uiState.update {
            it.copy(
                showPinSetup = show,
                showPinInput = "",
                showPinConfirm = "",
                pinStep = PinStep.ENTER
            )
        }
    }

    fun onPinInput(digit: String) {
        val s = _uiState.value
        when (s.pinStep) {
            PinStep.ENTER -> {
                val newPin = (s.showPinInput + digit).take(4)
                _uiState.update { it.copy(showPinInput = newPin) }
                if (newPin.length == 4) {
                    _uiState.update { it.copy(pinStep = PinStep.CONFIRM) }
                }
            }
            PinStep.CONFIRM -> {
                val newConfirm = (s.showPinConfirm + digit).take(4)
                _uiState.update { it.copy(showPinConfirm = newConfirm) }
                if (newConfirm.length == 4) {
                    if (newConfirm == s.showPinInput) {
                        savePin(newConfirm)
                    } else {
                        _uiState.update {
                            it.copy(
                                errorMessage   = "PINs don't match — try again",
                                showPinInput   = "",
                                showPinConfirm = "",
                                pinStep        = PinStep.ENTER
                            )
                        }
                    }
                }
            }
        }
    }

    fun onPinDelete() {
        val s = _uiState.value
        when (s.pinStep) {
            PinStep.ENTER   ->
                _uiState.update { it.copy(showPinInput = s.showPinInput.dropLast(1)) }
            PinStep.CONFIRM ->
                _uiState.update { it.copy(showPinConfirm = s.showPinConfirm.dropLast(1)) }
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch {
            prefs.setPinCode(pin)
            prefs.setPinEnabled(true)
            _uiState.update {
                it.copy(
                    showPinSetup   = false,
                    successMessage = "PIN set successfully! 🔐"
                )
            }
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            prefs.setPinEnabled(false)
            prefs.setPinCode("")
            _uiState.update { it.copy(successMessage = "PIN removed") }
        }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometricEnabled(enabled) }
    }

    fun setShowHidden(show: Boolean) {
        viewModelScope.launch { prefs.setShowHidden(show) }
    }

    // ── Backup ────────────────────────────────────────
    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            backupService.createBackup()
                .onSuccess { path ->
                    prefs.setLastBackup(System.currentTimeMillis())
                    _uiState.update {
                        it.copy(
                            isLoading      = false,
                            successMessage = "Backup saved! ✅"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            errorMessage = "Backup failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoBackup(enabled) }
    }

    fun loadBackupFiles() {
        viewModelScope.launch {
            val dir = context.getExternalFilesDir(
                android.os.Environment.DIRECTORY_DOCUMENTS
            )
            val lumioDir = java.io.File(dir, "Lumio")
            val files = lumioDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
            _uiState.update { it.copy(backupFiles = files, showRestoreDialog = true) }
        }
    }

    fun restoreBackup(filePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showRestoreDialog = false) }
            backupService.restoreBackup(filePath)
                .onSuccess { count ->
                    _uiState.update {
                        it.copy(
                            isLoading      = false,
                            successMessage = "Restored $count reminders! ✅"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            errorMessage = "Restore failed: ${e.message}"
                        )
                    }
                }
        }
    }

    // ── Language ──────────────────────────────────────
    fun setLanguage(lang: String) {
        viewModelScope.launch { prefs.setLanguage(lang) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

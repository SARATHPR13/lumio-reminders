package com.lumio.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "lumio_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_THEME_MODE        = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR     = booleanPreferencesKey("dynamic_color")
        val KEY_FONT_SIZE         = stringPreferencesKey("font_size")
        val KEY_DEFAULT_SOUND     = booleanPreferencesKey("default_sound")
        val KEY_DEFAULT_VIBRATION = booleanPreferencesKey("default_vibration")
        val KEY_PIN_ENABLED       = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_CODE          = stringPreferencesKey("pin_code")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_LANGUAGE          = stringPreferencesKey("language")
        val KEY_LAST_BACKUP       = longPreferencesKey("last_backup")
        val KEY_AUTO_BACKUP       = booleanPreferencesKey("auto_backup")
        val KEY_SHOW_HIDDEN       = booleanPreferencesKey("show_hidden")
        val KEY_FIRST_LAUNCH      = booleanPreferencesKey("first_launch")
        val KEY_SNOOZE_DEFAULT    = intPreferencesKey("snooze_default")
    }

    // ── Theme ─────────────────────────────────────────
    val themeMode: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            
                prefs[KEY_THEME_MODE] ?: "system"
            
        }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    val dynamicColor: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_DYNAMIC_COLOR] ?: true }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    // ── Font Size ─────────────────────────────────────
    val fontSize: Flow<FontSize> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            FontSize.valueOf(prefs[KEY_FONT_SIZE] ?: FontSize.MEDIUM.name)
        }

    suspend fun setFontSize(size: FontSize) {
        dataStore.edit { it[KEY_FONT_SIZE] = size.name }
    }

    // ── Notifications ────────────────────────────────
    val defaultSound: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_DEFAULT_SOUND] ?: true }

    suspend fun setDefaultSound(enabled: Boolean) {
        dataStore.edit { it[KEY_DEFAULT_SOUND] = enabled }
    }

    val defaultVibration: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_DEFAULT_VIBRATION] ?: true }

    suspend fun setDefaultVibration(enabled: Boolean) {
        dataStore.edit { it[KEY_DEFAULT_VIBRATION] = enabled }
    }

    val snoozeDefault: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_SNOOZE_DEFAULT] ?: 5 }

    suspend fun setSnoozeDefault(minutes: Int) {
        dataStore.edit { it[KEY_SNOOZE_DEFAULT] = minutes }
    }

    // ── Security ──────────────────────────────────────
    val pinEnabled: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_PIN_ENABLED] ?: false }

    suspend fun setPinEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_PIN_ENABLED] = enabled }
    }

    val pinCode: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_PIN_CODE] ?: "" }

    suspend fun setPinCode(pin: String) {
        dataStore.edit { it[KEY_PIN_CODE] = pin }
    }

    val biometricEnabled: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_BIOMETRIC_ENABLED] ?: false }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    val showHidden: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_SHOW_HIDDEN] ?: false }

    suspend fun setShowHidden(show: Boolean) {
        dataStore.edit { it[KEY_SHOW_HIDDEN] = show }
    }

    // ── Backup ────────────────────────────────────────
    val lastBackup: Flow<Long> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_LAST_BACKUP] ?: 0L }

    suspend fun setLastBackup(time: Long) {
        dataStore.edit { it[KEY_LAST_BACKUP] = time }
    }

    val autoBackup: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_AUTO_BACKUP] ?: false }

    suspend fun setAutoBackup(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_BACKUP] = enabled }
    }

    // ── Language ──────────────────────────────────────
    val language: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_LANGUAGE] ?: "English" }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    // ── First Launch ──────────────────────────────────
    val firstLaunch: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    suspend fun setFirstLaunch(first: Boolean) {
        dataStore.edit { it[KEY_FIRST_LAUNCH] = first }
    }
}

enum class FontSize(val label: String, val scale: Float) {
    SMALL ("Small",  0.85f),
    MEDIUM("Medium", 1.00f),
    LARGE ("Large",  1.15f),
    XLARGE("X-Large",1.30f)
}

package com.lumio.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lumio_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_THEME_MODE        = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR     = booleanPreferencesKey("dynamic_color")
        val KEY_DEFAULT_SOUND     = booleanPreferencesKey("default_sound")
        val KEY_DEFAULT_VIBRATION = booleanPreferencesKey("default_vibration")
        val KEY_BIOMETRIC         = booleanPreferencesKey("biometric")
        val KEY_FONT_SIZE         = stringPreferencesKey("font_size")
        val KEY_FIRST_LAUNCH      = booleanPreferencesKey("first_launch")
        val KEY_LANGUAGE          = stringPreferencesKey("language")
    }

    val themeMode: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_THEME_MODE] ?: "light" }

    val dynamicColors: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DYNAMIC_COLOR] ?: true }

    val defaultSound: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DEFAULT_SOUND] ?: true }

    val defaultVibration: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DEFAULT_VIBRATION] ?: true }

    val biometricEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_BIOMETRIC] ?: false }

    val fontSize: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FONT_SIZE] ?: "medium" }

    val firstLaunch: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    val language: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_LANGUAGE] ?: "en" }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDefaultSound(enabled: Boolean) {
        dataStore.edit { it[KEY_DEFAULT_SOUND] = enabled }
    }

    suspend fun setDefaultVibration(enabled: Boolean) {
        dataStore.edit { it[KEY_DEFAULT_VIBRATION] = enabled }
    }

    suspend fun setBiometric(enabled: Boolean) {
        dataStore.edit { it[KEY_BIOMETRIC] = enabled }
    }

    suspend fun setFontSize(size: String) {
        dataStore.edit { it[KEY_FONT_SIZE] = size }
    }

    suspend fun setFirstLaunch(value: Boolean) {
        dataStore.edit { it[KEY_FIRST_LAUNCH] = value }
    }

    suspend fun setLanguage(code: String) {
        dataStore.edit { it[KEY_LANGUAGE] = code }
    }
}

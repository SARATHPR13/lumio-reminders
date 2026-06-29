package com.lumio.app.data.preferences

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    val supportedLanguages = listOf(
        Language("en", "English",   "English"),
        Language("ml", "Malayalam", "മലയാളം"),
        Language("hi", "Hindi",     "हिंदी"),
        Language("ta", "Tamil",     "தமிழ்"),
        Language("te", "Telugu",    "తెలుగు"),
        Language("kn", "Kannada",   "ಕನ್ನಡ"),
        Language("bn", "Bengali",   "বাংলা"),
        Language("mr", "Marathi",   "मराठी"),
        Language("gu", "Gujarati",  "ગુજરાતી")
    )

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale  = Locale(languageCode)
        Locale.setDefault(locale)
        val config  = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getCurrentLanguageCode(context: Context): String {
        return context.resources.configuration.locales[0].language
    }
}

data class Language(
    val code: String,
    val nameEnglish: String,
    val nameNative: String
)

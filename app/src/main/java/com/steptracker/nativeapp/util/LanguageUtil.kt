package com.steptracker.nativeapp.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageUtil {

    private const val PREF_NAME = "language_pref"
    private const val KEY_LANGUAGE = "selected_language"

    fun applyLanguage(context: Context, languageCode: String): Context {
        if (languageCode.isEmpty()) return context

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun updateResource(context: Context, languageCode: String) {
        if (languageCode.isEmpty()) return

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "") ?: ""
    }

    fun isLanguageSelected(context: Context): Boolean {
        return getSavedLanguage(context).isNotEmpty()
    }
}

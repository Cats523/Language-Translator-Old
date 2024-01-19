package com.wonderapps.translator.screens

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val appLanguagePreferences =
            getSharedPreferences(MainActivity.LANGUAGE_PREFERENCE_KEY, MODE_PRIVATE)
        val languageCode =
            appLanguagePreferences.getString(MainActivity.LANGUAGE_PREFERENCE_VALUE_KEY, "") ?: "en"
        Log.d("defaultLanguageCode", "onCreate: $languageCode ")
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

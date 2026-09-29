package com.example.masterka.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class AppTheme {
    WORKSHOP,   // Мастерская — янтарь + кожа (текущая)
    MODERN,     // Современная — индиго + бирюза
    INDUSTRIAL  // Индустриальная — серый + оранжевый
}

object ThemePreferences {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    fun getTheme(context: Context): Flow<AppTheme> =
        context.dataStore.data.map { prefs ->
            val name = prefs[THEME_KEY] ?: AppTheme.WORKSHOP.name
            try {
                AppTheme.valueOf(name)
            } catch (e: Exception) {
                AppTheme.WORKSHOP
            }
        }

    suspend fun setTheme(context: Context, theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.name
        }
    }
}
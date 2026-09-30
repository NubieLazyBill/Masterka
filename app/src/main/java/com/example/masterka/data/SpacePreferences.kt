package com.example.masterka.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Активное помещение хранится в том же DataStore, что и тема.
 * Ключ: "active_space_id". Если == 0L, значит ещё не выбран — берём первый из БД.
 */
object SpacePreferences {

    private val ACTIVE_SPACE_KEY = longPreferencesKey("active_space_id")

    fun getActiveSpaceId(context: Context): Flow<Long> =
        context.dataStore.data.map { prefs ->
            prefs[ACTIVE_SPACE_KEY] ?: 0L
        }

    suspend fun setActiveSpaceId(context: Context, spaceId: Long) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_SPACE_KEY] = spaceId
        }
    }

    suspend fun getActiveSpaceIdOnce(context: Context): Long =
        getActiveSpaceId(context).first()
}
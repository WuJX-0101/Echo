package com.echo.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATION_ENABLED] ?: false
    }

    val notificationHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATION_HOUR] ?: 21
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: false
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setNotificationHour(hour: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_HOUR] = hour
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }
}

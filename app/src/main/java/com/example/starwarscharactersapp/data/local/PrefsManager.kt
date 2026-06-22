package com.example.starwarscharactersapp.data.local

import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.starwarscharactersapp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode(@param:StringRes val titleRes: Int) {
    SYSTEM(R.string.settings_screen_system_default_option),
    LIGHT(R.string.settings_screen_light_option),
    DARK(R.string.settings_screen_dark_option),
}

@Singleton
class PrefsManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private object Keys {
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val THEME_MODE = intPreferencesKey("theme_mode")
    }

    val lastSyncTimestamp: Flow<Long> = getPreference(Keys.LAST_SYNC_TIMESTAMP, 0L)

    val themeMode: Flow<ThemeMode> = getPreference(Keys.THEME_MODE, ThemeMode.SYSTEM.ordinal)
        .map { modeIndex ->
            ThemeMode.entries.getOrElse(modeIndex) { ThemeMode.SYSTEM }
        }

    private fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences -> preferences[key] ?: defaultValue }
            .distinctUntilChanged()

    suspend fun updateSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.ordinal
        }
    }
}

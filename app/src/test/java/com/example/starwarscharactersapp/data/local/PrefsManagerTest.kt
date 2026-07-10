package com.example.starwarscharactersapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

private class FakeDataStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

private class ThrowingDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { throw IOException("disk error") }
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        throw UnsupportedOperationException()
}

class PrefsManagerTest {

    private lateinit var dataStore: FakeDataStore
    private lateinit var prefsManager: PrefsManager

    @Before
    fun setup() {
        dataStore = FakeDataStore()
        prefsManager = PrefsManager(dataStore)
    }

    @Test
    fun `lastSyncTimestamp defaults to zero`() = runTest {
        assertEquals(0L, prefsManager.lastSyncTimestamp.first())
    }

    @Test
    fun `updateSyncTimestamp persists and is reflected in lastSyncTimestamp`() = runTest {
        prefsManager.updateSyncTimestamp(12345L)

        assertEquals(12345L, prefsManager.lastSyncTimestamp.first())
    }

    @Test
    fun `themeMode defaults to SYSTEM`() = runTest {
        assertEquals(ThemeMode.SYSTEM, prefsManager.themeMode.first())
    }

    @Test
    fun `updateThemeMode persists and is reflected in themeMode`() = runTest {
        prefsManager.updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, prefsManager.themeMode.first())
    }

    @Test
    fun `themeMode falls back to SYSTEM for an out-of-range stored ordinal`() = runTest {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[androidx.datastore.preferences.core.intPreferencesKey("theme_mode")] = 3
            }
        }

        assertEquals(ThemeMode.SYSTEM, prefsManager.themeMode.first())
    }

    @Test
    fun `lastSyncTimestamp falls back to default when the DataStore throws IOException`() = runTest {
        val throwingPrefsManager = PrefsManager(ThrowingDataStore())

        assertEquals(0L, throwingPrefsManager.lastSyncTimestamp.first())
    }
}

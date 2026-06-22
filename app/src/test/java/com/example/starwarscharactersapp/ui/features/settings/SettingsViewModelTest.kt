package com.example.starwarscharactersapp.ui.features.settings

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.data.local.ThemeMode
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val prefsManager = mockk<PrefsManager>(relaxed = true)
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { prefsManager.themeMode } returns themeModeFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `themeMode reflects prefsManager value`() = runTest {
        // Act
        viewModel = SettingsViewModel(prefsManager)
        
        // Assert
        viewModel.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            themeModeFlow.value = ThemeMode.DARK
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun `setThemeMode calls prefsManager update`() = runTest {
        // Act
        viewModel = SettingsViewModel(prefsManager)
        viewModel.setThemeMode(ThemeMode.LIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { prefsManager.updateThemeMode(ThemeMode.LIGHT) }
    }
}

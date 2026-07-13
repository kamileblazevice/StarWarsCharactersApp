package com.example.starwarscharactersapp

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.data.local.ThemeMode
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
class MainViewModelTest {

    private val prefsManager = mockk<PrefsManager>(relaxed = true)
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel

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
    fun `themeMode defaults to null while loading then reflects prefsManager updates`() = runTest {
        viewModel = MainViewModel(prefsManager)

        viewModel.themeMode.test {
            assertEquals(null, awaitItem())
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            themeModeFlow.value = ThemeMode.LIGHT
            assertEquals(ThemeMode.LIGHT, awaitItem())
        }
    }
}

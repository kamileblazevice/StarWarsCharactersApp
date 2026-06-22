package com.example.starwarscharactersapp.ui.features.splash

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val repository = mockk<StarWarsRepository>()
    private val prefsManager = mockk<PrefsManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SplashViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { prefsManager.lastSyncTimestamp } returns flowOf(0L)
        coEvery { repository.syncAllData() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isDataLoaded becomes true after sync`() = runTest {
        // Act
        viewModel = SplashViewModel(repository, prefsManager)
        
        // Assert
        viewModel.isDataLoaded.test {
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
        }
    }
}

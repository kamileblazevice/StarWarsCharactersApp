package com.example.starwarscharactersapp.ui.features.splash

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.domain.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.SyncProgress
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        every { repository.syncAllData() } returns flowOf(SyncProgress.Success)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isDataLoaded becomes true after sync`() = runTest {
        viewModel = SplashViewModel(repository, prefsManager)

        viewModel.isDataLoaded.test {
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
        }

        verify { repository.syncAllData() }
        coVerify { prefsManager.updateSyncTimestamp(any()) }
    }

    @Test
    fun `sync is skipped when last sync was recent`() = runTest {
        every { prefsManager.lastSyncTimestamp } returns flowOf(System.currentTimeMillis())

        viewModel = SplashViewModel(repository, prefsManager)

        viewModel.isDataLoaded.test {
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
        }
        verify(exactly = 0) { repository.syncAllData() }
        coVerify(exactly = 0) { prefsManager.updateSyncTimestamp(any()) }
    }

    @Test
    fun `sync timestamp is not updated when syncAllData fails`() = runTest {
        every { repository.syncAllData() } returns flowOf(SyncProgress.Failure)

        viewModel = SplashViewModel(repository, prefsManager)

        viewModel.isDataLoaded.test {
            assertEquals(false, awaitItem())
            assertEquals(true, awaitItem())
        }
        coVerify(exactly = 0) { prefsManager.updateSyncTimestamp(any()) }
    }

    @Test
    fun `syncProgress reflects InProgress emissions before completing`() = runTest {
        every { repository.syncAllData() } returns flowOf(
            SyncProgress.InProgress(completed = 1, total = 2),
            SyncProgress.InProgress(completed = 2, total = 2),
            SyncProgress.Success,
        )

        viewModel = SplashViewModel(repository, prefsManager)

        viewModel.syncProgress.test {
            assertEquals(null, awaitItem())
            assertEquals(SyncProgress.InProgress(1, 2), awaitItem())
            assertEquals(SyncProgress.InProgress(2, 2), awaitItem())
            assertEquals(SyncProgress.Success, awaitItem())
        }
    }
}

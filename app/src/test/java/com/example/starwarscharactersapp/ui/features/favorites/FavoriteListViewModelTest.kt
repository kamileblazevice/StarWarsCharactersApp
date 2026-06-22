package com.example.starwarscharactersapp.ui.features.favorites

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.helper.UiState
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteListViewModelTest {

    private val repository = mockk<StarWarsRepository>()
    private val networkMonitor = mockk<NetworkMonitor>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: FavoriteListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel observes favorites and updates state`() = runTest {
        // Arrange
        val favorites = listOf(StarWarsCharacter(id = "1", name = "Luke Skywalker", isFavorite = true))
        every { repository.getFavoriteCharacters() } returns flowOf(favorites)

        // Act
        viewModel = FavoriteListViewModel(repository, networkMonitor)

        // Assert
        viewModel.state.test {
            // Initial state is Loading
            assertEquals(UiState.Loading, awaitItem())

            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(1, (state as UiState.Success).data.size)
            assertEquals("Luke Skywalker", state.data[0].name)
        }
    }
}

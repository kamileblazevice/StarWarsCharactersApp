package com.example.starwarscharactersapp.ui.features.list

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.helper.UiState
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {

    private val repository = mockk<StarWarsRepository>()
    private val networkMonitor = mockk<NetworkMonitor>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CharacterListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getCharactersFlow() } returns flowOf(emptyList())
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { repository.getCharacters() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Success`() = runTest {
        // Arrange
        val characters = listOf(StarWarsCharacter(id = "1", name = "Luke Skywalker"))
        every { repository.getCharactersFlow() } returns flowOf(characters)
        
        // Act
        viewModel = CharacterListViewModel(repository, networkMonitor)

        // Assert
        viewModel.state.test {
            // Initial state is Loading
            assertEquals(UiState.Loading, awaitItem())
            
            // Then transitions to Success
            val state = awaitItem()
            assertTrue(state is UiState.Success)
        }
    }
}

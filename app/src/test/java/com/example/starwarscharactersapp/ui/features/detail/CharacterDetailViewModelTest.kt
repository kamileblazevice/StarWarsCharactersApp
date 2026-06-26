package com.example.starwarscharactersapp.ui.features.detail

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
class CharacterDetailViewModelTest {

    private val repository = mockk<StarWarsRepository>()
    private val networkMonitor = mockk<NetworkMonitor>()
    private val testDispatcher = StandardTestDispatcher()
    private val characterId = "1"

    private lateinit var viewModel: CharacterDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns flowOf(false)
        every { repository.getCharacterFlow(any()) } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Success`() = runTest {
        // Arrange
        val character = StarWarsCharacter(id = characterId, name = "Luke Skywalker")
        coEvery { repository.getCharacter(characterId) } returns character

        // Act
        viewModel = CharacterDetailViewModel(repository, networkMonitor, characterId)

        // Assert
        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals("Luke Skywalker", (state as UiState.Success).data.character.name)
        }
    }

    @Test
    fun `state is Error when API fails and no local data`() = runTest {
        // Arrange
        coEvery { repository.getCharacter(characterId) } returns null

        // Act
        viewModel = CharacterDetailViewModel(repository, networkMonitor, characterId)

        // Assert
        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is UiState.Error)
        }
    }
}

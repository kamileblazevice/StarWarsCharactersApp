package com.example.starwarscharactersapp.ui.features.list

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.list.model.CharacterListEvent
import com.example.starwarscharactersapp.ui.helper.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
        val characters = listOf(StarWarsCharacter(id = "1", name = "Luke Skywalker"))
        every { repository.getCharactersFlow() } returns flowOf(characters)

        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is UiState.Success)
        }
    }

    @Test
    fun `state becomes Error when both cache and network are empty`() = runTest {
        coEvery { repository.getCharacters() } returns null

        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            assertTrue(awaitItem() is UiState.Error)
        }
    }

    @Test
    fun `search query filters and sorts characters by name`() = runTest {
        val characters = listOf(
            StarWarsCharacter(id = "1", name = "Leia Organa"),
            StarWarsCharacter(id = "2", name = "Luke Skywalker"),
        )
        every { repository.getCharactersFlow() } returns flowOf(characters)
        coEvery { repository.getCharacters() } returns characters

        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            val initial = awaitItem() as UiState.Success
            assertEquals(2, initial.data.size)

            viewModel.onEvent(CharacterListEvent.OnSearchQueryChanged("Luke"))

            val filtered = awaitItem() as UiState.Success
            assertEquals(1, filtered.data.size)
            assertEquals("Luke Skywalker", filtered.data[0].name)
        }
    }

    @Test
    fun `search query matching nothing returns an empty list`() = runTest {
        val characters = listOf(StarWarsCharacter(id = "1", name = "Leia Organa"))
        every { repository.getCharactersFlow() } returns flowOf(characters)
        coEvery { repository.getCharacters() } returns characters

        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            assertTrue(awaitItem() is UiState.Success)

            viewModel.onEvent(CharacterListEvent.OnSearchQueryChanged("Vader"))

            val filtered = awaitItem() as UiState.Success
            assertTrue(filtered.data.isEmpty())
        }
    }

    @Test
    fun `OnToggleFavorite event delegates to repository`() = runTest {
        coEvery { repository.getCharacters() } returns emptyList()
        coEvery { repository.toggleFavorite(any()) } returns Unit
        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.onEvent(CharacterListEvent.OnToggleFavorite("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.toggleFavorite("1") }
    }

    @Test
    fun `OnReloadData event triggers a network refresh`() = runTest {
        coEvery { repository.getCharacters() } returns emptyList()
        coEvery { repository.refreshCharactersFromNetwork() } returns listOf(
            StarWarsCharacter(id = "1", name = "Luke Skywalker"),
        )
        viewModel = CharacterListViewModel(repository, networkMonitor)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CharacterListEvent.OnReloadData)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.refreshCharactersFromNetwork() }
        assertTrue(viewModel.state.value !is UiState.Error)
    }

    @Test
    fun `network reconnect triggers a reload while in Error state`() = runTest {
        coEvery { repository.getCharacters() } returns null
        val isOnlineFlow = MutableSharedFlow<Boolean>(replay = 1)
        isOnlineFlow.tryEmit(false)
        every { networkMonitor.isOnline } returns isOnlineFlow
        coEvery { repository.refreshCharactersFromNetwork() } returns emptyList()

        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            assertTrue(awaitItem() is UiState.Error)

            isOnlineFlow.emit(true)
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.refreshCharactersFromNetwork() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `imageReloadRevision increments on network status changes while not in Error state`() = runTest {
        coEvery { repository.getCharacters() } returns emptyList()
        every { networkMonitor.isOnline } returns flowOf(true, false, true)

        viewModel = CharacterListViewModel(repository, networkMonitor)

        viewModel.imageReloadRevision.test {
            assertEquals(0, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
        }
    }
}

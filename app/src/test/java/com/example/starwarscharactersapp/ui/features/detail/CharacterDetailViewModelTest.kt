package com.example.starwarscharactersapp.ui.features.detail

import app.cash.turbine.test
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.domain.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.Vehicle
import com.example.starwarscharactersapp.ui.features.detail.model.CharacterDetailEvent
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {

    private val repository = mockk<StarWarsRepository>()
    private val networkMonitor = mockk<NetworkMonitor>()
    private val testDispatcher = StandardTestDispatcher()

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
        val character = createStarWarsCharacter()
        coEvery { repository.getCharacter("1") } returns character

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals("Luke Skywalker", (state as UiState.Success).data.character.name)
        }
    }

    @Test
    fun `state is Error when API fails and no local data`() = runTest {
        coEvery { repository.getCharacter("1") } returns null

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")

        viewModel.state.test {
            assertEquals(UiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is UiState.Error)
        }
    }

    @Test
    fun `loads related planet, films, starships and vehicles on success`() = runTest {
        val character = createStarWarsCharacter().copy(
            homeworld = "https://swapi.dev/api/planets/1/",
            filmUrls = listOf("https://swapi.dev/api/films/1/"),
            starshipUrls = listOf("https://swapi.dev/api/starships/12/"),
            vehicleUrls = listOf("https://swapi.dev/api/vehicles/14/"),
            )
        coEvery { repository.getCharacter("1") } returns character
        coEvery { repository.getPlanet("1") } returns Planet(id = "1", name = "Tatooine")
        coEvery { repository.getFilm("1") } returns Film(id = "1", title = "A New Hope")
        coEvery { repository.getStarship("12") } returns Starship(id = "12", name = "X-wing")
        coEvery { repository.getVehicle("14") } returns Vehicle(id = "14", name = "Snowspeeder")

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertEquals("Tatooine", state.character.planet?.name)
        assertEquals(listOf(Film(id = "1", title = "A New Hope")), state.character.films)
        assertEquals(listOf(Starship(id = "12", name = "X-wing")), state.character.starships)
        assertEquals(listOf(Vehicle(id = "14", name = "Snowspeeder")), state.character.vehicles)
        assertFalse(state.planetError)
        assertFalse(state.filmsError)
        assertFalse(state.starshipsError)
        assertFalse(state.vehiclesError)
    }

    @Test
    fun `planetError is set when the planet fetch fails`() = runTest {
        val character = createStarWarsCharacter().copy(homeworld = "https://swapi.dev/api/planets/1/")
        coEvery { repository.getCharacter("1") } returns character
        coEvery { repository.getPlanet("1") } returns null

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertTrue(state.planetError)
        assertEquals(null, state.character.planet)
    }

    @Test
    fun `filmsError is not set when at least one film fetch succeeds`() = runTest {
        val character = createStarWarsCharacter().copy(
            filmUrls = listOf(
                "https://swapi.dev/api/films/1/",
                "https://swapi.dev/api/films/2/",
            )
        )
        coEvery { repository.getCharacter("1") } returns character
        coEvery { repository.getFilm("1") } returns Film(id = "1", title = "A New Hope")
        coEvery { repository.getFilm("2") } returns null

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertFalse(state.filmsError)
        assertEquals(listOf(Film(id = "1", title = "A New Hope")), state.character.films)
    }

    @Test
    fun `filmsError is set when every film fetch fails`() = runTest {
        val character = createStarWarsCharacter().copy(filmUrls = listOf("https://swapi.dev/api/films/1/"))
        coEvery { repository.getCharacter("1") } returns character
        coEvery { repository.getFilm("1") } returns null

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertTrue(state.filmsError)
        assertTrue(state.character.films.isEmpty())
    }

    @Test
    fun `OnToggleFavorite event delegates to repository`() = runTest {
        coEvery { repository.getCharacter("1") } returns createStarWarsCharacter()
        coEvery { repository.toggleFavorite("1") } returns Unit
        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")

        viewModel.onEvent(CharacterDetailEvent.OnToggleFavorite)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.toggleFavorite("1") }
    }

    @Test
    fun `observeCharacter reflects favorite status changes`() = runTest {
        val character = createStarWarsCharacter().copy(isFavorite = false)
        coEvery { repository.getCharacter("1") } returns character
        val characterFlow = MutableSharedFlow<StarWarsCharacter?>(replay = 1)
        every { repository.getCharacterFlow("1") } returns characterFlow

        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()
        characterFlow.emit(character.copy(isFavorite = true))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertTrue(state.character.isFavorite)
    }

    @Test
    fun `OnRetryPlanet reloads the planet after a failure`() = runTest {
        val character = createStarWarsCharacter().copy(homeworld = "https://swapi.dev/api/planets/1/")
        coEvery { repository.getCharacter("1") } returns character
        coEvery { repository.getPlanet("1") } returns null
        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((viewModel.state.value as UiState.Success).data.planetError)

        coEvery { repository.getPlanet("1") } returns Planet(id = "1", name = "Tatooine")
        viewModel.onEvent(CharacterDetailEvent.OnRetryPlanet)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertFalse(state.planetError)
        assertEquals("Tatooine", state.character.planet?.name)
    }

    @Test
    fun `network reconnect retries all failed sections`() = runTest {
        val character = createStarWarsCharacter().copy(
            homeworld = "https://swapi.dev/api/planets/1/",
            filmUrls = listOf("https://swapi.dev/api/films/1/"),
        )
        coEvery { repository.getCharacter("1") } returns character
        coEvery { repository.getPlanet("1") } returns null
        coEvery { repository.getFilm("1") } returns null
        val isOnlineFlow = MutableSharedFlow<Boolean>(replay = 1)
        isOnlineFlow.tryEmit(false)
        every { networkMonitor.isOnline } returns isOnlineFlow
        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue((viewModel.state.value as UiState.Success).data.planetError)
        assertTrue((viewModel.state.value as UiState.Success).data.filmsError)

        coEvery { repository.getPlanet("1") } returns Planet(id = "1", name = "Tatooine")
        coEvery { repository.getFilm("1") } returns Film(id = "1", title = "A New Hope")
        isOnlineFlow.emit(true)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = (viewModel.state.value as UiState.Success).data
        assertFalse(state.planetError)
        assertFalse(state.filmsError)
        assertEquals("Tatooine", state.character.planet?.name)
        assertEquals("A New Hope", state.character.films[0].title)
    }

    @Test
    fun `OnReloadData event allows recovery from an Error state`() = runTest {
        coEvery { repository.getCharacter("1") } returns null
        viewModel = CharacterDetailViewModel(repository, networkMonitor, "1")
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { repository.getCharacter("1") } returns StarWarsCharacter(id = "1", name = "Luke Skywalker")
        viewModel.onEvent(CharacterDetailEvent.OnReloadData)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is UiState.Success)
        assertEquals("Luke Skywalker", (state as UiState.Success).data.character.name)
    }
}

private fun createStarWarsCharacter() = StarWarsCharacter(
    id = "1",
    name = "Luke Skywalker",
)

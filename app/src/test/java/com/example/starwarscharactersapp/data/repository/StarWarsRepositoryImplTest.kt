package com.example.starwarscharactersapp.data.repository

import com.example.starwarscharactersapp.data.local.StarWarsDao
import com.example.starwarscharactersapp.data.local.entity.CharacterEntity
import com.example.starwarscharactersapp.data.local.entity.FilmEntity
import com.example.starwarscharactersapp.data.local.entity.PlanetEntity
import com.example.starwarscharactersapp.data.local.entity.StarshipEntity
import com.example.starwarscharactersapp.data.local.entity.VehicleEntity
import com.example.starwarscharactersapp.data.model.DatabankCharacterDto
import com.example.starwarscharactersapp.data.model.FilmDto
import com.example.starwarscharactersapp.data.model.PlanetDto
import com.example.starwarscharactersapp.data.model.StarWarsCharacterDto
import com.example.starwarscharactersapp.data.model.StarshipDto
import com.example.starwarscharactersapp.data.model.VehicleDto
import com.example.starwarscharactersapp.data.network.DatabankApiService
import com.example.starwarscharactersapp.data.network.SwapiApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class StarWarsRepositoryImplTest {

    private val api = mockk<SwapiApiService>()
    private val databankApi = mockk<DatabankApiService>()
    private val dao = mockk<StarWarsDao>(relaxed = true)
    private lateinit var repository: StarWarsRepositoryImpl

    @Before
    fun setup() {
        repository = StarWarsRepositoryImpl(api, databankApi, dao)
    }

    @Test
    fun `getCharacters returns local data immediately without API call when cache is not empty`() = runTest {
        val localCharacter = createCharacterEntity()
        every { dao.getCharacters() } returns flowOf(listOf(localCharacter))

        val result = repository.getCharacters()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Luke Skywalker", result[0].name)
        coVerify(exactly = 0) { api.getCharacters() }
        coVerify(exactly = 0) { databankApi.getCharacterByName(any()) }
    }

    @Test
    fun `getCharacters fetches from network and saves to DB when cache is empty`() = runTest {
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val databankDto = DatabankCharacterDto(name = "Luke Skywalker", image = "image_url", description = "desc")
        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        coEvery { databankApi.getCharacterByName("Luke Skywalker") } returns Response.success(listOf(databankDto))
        every { dao.getCharacters() } returns flowOf(emptyList())

        val result = repository.getCharacters()

        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Luke Skywalker", result[0].name)
        assertEquals("image_url", result[0].imageUrl)
        coVerify { dao.insertCharacters(any()) }
    }

    @Test
    fun `getCharacters skips Databank API when imageUrl is already cached`() = runTest {
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val cachedEntity = createCharacterEntity().copy(imageUrl = "cached_url", description = "cached_desc")
        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        every { dao.getCharacters() } returns flowOf(listOf(cachedEntity))

        val result = repository.getCharacters()

        assertNotNull(result)
        assertEquals("cached_url", result!![0].imageUrl)
        coVerify(exactly = 0) { api.getCharacters() }
        coVerify(exactly = 0) { databankApi.getCharacterByName(any()) }
    }

    @Test
    fun `getCharacters returns null when network fails and DB is empty`() = runTest {
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(emptyList())

        val result = repository.getCharacters()

        assertNull(result)
    }

    @Test
    fun `refreshCharactersFromNetwork always calls network even when cache is not empty`() = runTest {
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val cachedEntity = createCharacterEntity().copy(imageUrl = "cached_url", description = "cached_desc")
        val databankDto = DatabankCharacterDto(name = "Luke Skywalker", image = "cached_url", description = "cached_desc")
        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        coEvery { databankApi.getCharacterByName(any()) } returns Response.success(listOf(databankDto))
        every { dao.getCharacters() } returns flowOf(listOf(cachedEntity))

        val result = repository.refreshCharactersFromNetwork()

        assertNotNull(result)
        coVerify(exactly = 1) { api.getCharacters() }
        coVerify { dao.insertCharacters(any()) }
    }

    @Test
    fun `refreshCharactersFromNetwork returns null when network fails`() = runTest {
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(emptyList())

        val result = repository.refreshCharactersFromNetwork()

        assertNull(result)
    }

    @Test
    fun `getCharacter returns local data without hitting the network when cached`() = runTest {
        val cachedEntity = createCharacterEntity()
        coEvery { dao.getCharacterById("1") } returns cachedEntity

        val result = repository.getCharacter("1")

        assertNotNull(result)
        assertEquals("Luke Skywalker", result!!.name)
        coVerify(exactly = 0) { api.getCharacter(any()) }
    }

    @Test
    fun `getCharacter fetches from network, enriches and caches when not local`() = runTest {
        val characterDto = StarWarsCharacterDto(name = "Leia Organa", url = "https://swapi.dev/api/people/5/")
        val databankDto = DatabankCharacterDto(name = "Leia Organa", image = "leia.png", description = "princess")
        coEvery { dao.getCharacterById("5") } returns null
        coEvery { api.getCharacter("5") } returns Response.success(characterDto)
        coEvery { databankApi.getCharacterByName("Leia Organa") } returns Response.success(listOf(databankDto))

        val result = repository.getCharacter("5")

        assertNotNull(result)
        assertEquals("leia.png", result!!.imageUrl)
        assertEquals("princess", result.description)
        assertFalse(result.isFavorite)
        coVerify { dao.insertCharacters(match { it.size == 1 && it[0].id == "5" }) }
    }

    @Test
    fun `getCharacter returns null when both cache and network miss`() = runTest {
        coEvery { dao.getCharacterById("9") } returns null
        coEvery { api.getCharacter("9") } throws Exception("Network error")

        val result = repository.getCharacter("9")

        assertNull(result)
    }

    @Test
    fun `getCharacter leaves imageUrl null when Databank has no match`() = runTest {
        val characterDto = StarWarsCharacterDto(name = "Unknown", url = "https://swapi.dev/api/people/7/")
        coEvery { dao.getCharacterById("7") } returns null
        coEvery { api.getCharacter("7") } returns Response.success(characterDto)
        coEvery { databankApi.getCharacterByName("Unknown") } returns Response.success(emptyList())

        val result = repository.getCharacter("7")

        assertNotNull(result)
        assertNull(result!!.imageUrl)
    }


    @Test
    fun `toggleFavorite delegates to dao`() = runTest {
        repository.toggleFavorite("1")

        coVerify { dao.toggleFavoriteStatus("1") }
    }

    @Test
    fun `getCharacterFlow maps null to null`() = runTest {
        every { dao.getCharacterFlowById("1") } returns flowOf(null)

        val result = repository.getCharacterFlow("1")

        assertNull(result.first())
    }

    @Test
    fun `getCharactersFlow maps dao flow list to domain models`() = runTest {
        every { dao.getCharacters() } returns flowOf(listOf(createCharacterEntity(), createCharacterEntity(id = "2")))

        val result = repository.getCharactersFlow().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `getFavoriteCharacters maps dao flow to domain models`() = runTest {
        val favorite = createCharacterEntity().copy(isFavorite = true)
        every { dao.getFavoriteCharacters() } returns flowOf(listOf(favorite))

        val result = repository.getFavoriteCharacters().first()

        assertEquals(1, result.size)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `getPlanet returns local data without hitting the network when cached`() = runTest {
        coEvery { dao.getPlanetById("1") } returns createPlanetEntity()

        val result = repository.getPlanet("1")

        assertNotNull(result)
        coVerify(exactly = 0) { api.getPlanet(any()) }
    }

    @Test
    fun `getPlanet fetches from network and caches when not local`() = runTest {
        coEvery { dao.getPlanetById("1") } returns null
        coEvery { api.getPlanet("1") } returns Response.success(createPlanetDto())

        val result = repository.getPlanet("1")

        assertNotNull(result)
        assertEquals("Tatooine", result!!.name)
        coVerify { dao.insertPlanet(any()) }
    }

    @Test
    fun `getPlanet returns null when both cache and network miss`() = runTest {
        coEvery { dao.getPlanetById("1") } returns null
        coEvery { api.getPlanet("1") } throws Exception("Network error")

        val result = repository.getPlanet("1")

        assertNull(result)
    }

    @Test
    fun `getFilm returns local data without hitting the network when cached`() = runTest {
        coEvery { dao.getFilmById("1") } returns createFilmEntity()

        val result = repository.getFilm("1")

        assertNotNull(result)
        coVerify(exactly = 0) { api.getFilm(any()) }
    }

    @Test
    fun `getFilm fetches from network and caches when not local`() = runTest {
        coEvery { dao.getFilmById("1") } returns null
        coEvery { api.getFilm("1") } returns Response.success(createFilmDto())

        val result = repository.getFilm("1")

        assertNotNull(result)
        assertEquals("A New Hope", result!!.title)
        coVerify { dao.insertFilm(any()) }
    }

    @Test
    fun `getFilm returns null when both cache and network miss`() = runTest {
        coEvery { dao.getFilmById("1") } returns null
        coEvery { api.getFilm("1") } throws Exception("Network error")

        val result = repository.getFilm("1")

        assertNull(result)
    }

    @Test
    fun `getStarship returns local data without hitting the network when cached`() = runTest {
        coEvery { dao.getStarshipById("12") } returns createStarshipEntity()

        val result = repository.getStarship("12")

        assertNotNull(result)
        coVerify(exactly = 0) { api.getStarship(any()) }
    }

    @Test
    fun `getStarship fetches from network and caches when not local`() = runTest {
        coEvery { dao.getStarshipById("12") } returns null
        coEvery { api.getStarship("12") } returns Response.success(createStarshipDto())

        val result = repository.getStarship("12")

        assertNotNull(result)
        assertEquals("X-wing", result!!.name)
        coVerify { dao.insertStarship(any()) }
    }

    @Test
    fun `getStarship returns null when both cache and network miss`() = runTest {
        coEvery { dao.getStarshipById("12") } returns null
        coEvery { api.getStarship("12") } throws Exception("Network error")

        val result = repository.getStarship("12")

        assertNull(result)
    }

    @Test
    fun `getVehicle returns local data without hitting the network when cached`() = runTest {
        coEvery { dao.getVehicleById("14") } returns createVehicleEntity()

        val result = repository.getVehicle("14")

        assertNotNull(result)
        coVerify(exactly = 0) { api.getVehicle(any()) }
    }

    @Test
    fun `getVehicle fetches from network and caches when not local`() = runTest {
        coEvery { dao.getVehicleById("14") } returns null
        coEvery { api.getVehicle("14") } returns Response.success(createVehicleDto())

        val result = repository.getVehicle("14")

        assertNotNull(result)
        assertEquals("Snowspeeder", result!!.name)
        coVerify { dao.insertVehicle(any()) }
    }

    @Test
    fun `getVehicle returns null when both cache and network miss`() = runTest {
        coEvery { dao.getVehicleById("14") } returns null
        coEvery { api.getVehicle("14") } throws Exception("Network error")

        val result = repository.getVehicle("14")

        assertNull(result)
    }

    @Test
    fun `syncAllData returns false when character refresh fails`() = runTest {
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(emptyList())

        val result = repository.syncAllData()

        assertFalse(result)
    }

    @Test
    fun `syncAllData refreshes characters then fetches related planets, films, starships and vehicles`() = runTest {
        val characterDto = StarWarsCharacterDto(
            name = "Luke Skywalker",
            url = "https://swapi.dev/api/people/1/",
            homeworld = "https://swapi.dev/api/planets/1/",
            filmUrls = listOf("https://swapi.dev/api/films/1/"),
            starshipUrls = listOf("https://swapi.dev/api/starships/12/"),
            vehicleUrls = listOf("https://swapi.dev/api/vehicles/14/"),
        )
        val databankDto = DatabankCharacterDto(name = "Luke Skywalker", image = "image_url", description = "desc")
        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        coEvery { databankApi.getCharacterByName(any()) } returns Response.success(listOf(databankDto))
        every { dao.getCharacters() } returns flowOf(emptyList())

        coEvery { dao.getPlanetById("1") } returns null
        coEvery { api.getPlanet("1") } returns Response.success(createPlanetDto())
        coEvery { dao.getFilmById("1") } returns null
        coEvery { api.getFilm("1") } returns Response.success(createFilmDto())
        coEvery { dao.getStarshipById("12") } returns null
        coEvery { api.getStarship("12") } returns Response.success(createStarshipDto())
        coEvery { dao.getVehicleById("14") } returns null
        coEvery { api.getVehicle("14") } returns Response.success(createVehicleDto())

        assertTrue(repository.syncAllData())
        coVerify { dao.insertPlanet(any()) }
        coVerify { dao.insertFilm(any()) }
        coVerify { dao.insertStarship(any()) }
        coVerify { dao.insertVehicle(any()) }
    }

    private fun createPlanetDto() = PlanetDto(
        climate = "arid",
        diameter = "10465",
        gravity = "1 standard",
        name = "Tatooine",
        population = "200000",
        terrain = "desert",
        url = "https://swapi.dev/api/planets/1/",
    )

    private fun createPlanetEntity() = PlanetEntity(
        id = "1",
        url = "url",
        name = "Tatooine",
        climate = "arid",
        diameter = "10465",
        gravity = "1 standard",
        population = "200000",
        terrain = "desert",
    )

    private fun createFilmEntity() = FilmEntity(
        id = "1",
        url = "url",
        title = "A New Hope",
        director = "George Lucas",
        releaseYear = "1977",
        )

    private fun createFilmDto() = FilmDto(
        title = "A New Hope",
        url = "https://swapi.dev/api/films/1/",
        director = "George Lucas",
        releaseDate = "1977-05-25",
        )

    private fun createStarshipEntity() = StarshipEntity(
        id = "12",
        url = "url",
        name = "X-wing",
        )

    private fun createStarshipDto() = StarshipDto(
        name = "X-wing",
        url = "https://swapi.dev/api/starships/12/",
        )

    private fun createVehicleEntity() = VehicleEntity(
        id = "14",
        url = "url",
        name = "Snowspeeder",
        )

    private fun createVehicleDto() = VehicleDto(
        name = "Snowspeeder",
        url = "https://swapi.dev/api/vehicles/14/",
    )

    private fun createCharacterEntity(id: String = "1", name: String = "Luke Skywalker") = CharacterEntity(
        id = id,
        url = "https://swapi.dev/api/people/$id/",
        name = name,
        birthYear = "19BBY",
        eyeColor = "blue",
        gender = "male",
        hairColor = "blond",
        height = "172",
        homeworld = "https://swapi.dev/api/planets/1/",
        mass = "77",
        skinColor = "fair",
        filmUrls = emptyList(),
        starshipUrls = emptyList(),
        vehicleUrls = emptyList(),
    )
}

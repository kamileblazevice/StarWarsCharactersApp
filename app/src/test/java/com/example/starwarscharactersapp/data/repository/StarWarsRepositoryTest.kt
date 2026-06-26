package com.example.starwarscharactersapp.data.repository

import com.example.starwarscharactersapp.data.local.StarWarsDao
import com.example.starwarscharactersapp.data.local.entity.CharacterEntity
import com.example.starwarscharactersapp.data.model.DatabankCharacterDto
import com.example.starwarscharactersapp.data.model.StarWarsCharacterDto
import com.example.starwarscharactersapp.data.network.DatabankApiService
import com.example.starwarscharactersapp.data.network.SwapiApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class StarWarsRepositoryTest {

    private val api = mockk<SwapiApiService>()
    private val databankApi = mockk<DatabankApiService>()
    private val dao = mockk<StarWarsDao>(relaxed = true)
    private lateinit var repository: StarWarsRepository

    @Before
    fun setup() {
        repository = StarWarsRepository(api, databankApi, dao)
    }

    // getCharacters() — offline-first behaviour

    @Test
    fun `getCharacters returns local data immediately without API call when cache is not empty`() = runTest {
        // Arrange
        val localCharacter = createCharacterEntity(id = "1", name = "Luke Skywalker")
        every { dao.getCharacters() } returns flowOf(listOf(localCharacter))

        // Act
        val result = repository.getCharacters()

        // Assert
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Luke Skywalker", result[0].name)
        coVerify(exactly = 0) { api.getCharacters() }
        coVerify(exactly = 0) { databankApi.getCharacterByName(any()) }
    }

    @Test
    fun `getCharacters fetches from network and saves to DB when cache is empty`() = runTest {
        // Arrange
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val databankDto = DatabankCharacterDto(name = "Luke Skywalker", image = "image_url", description = "desc")

        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        coEvery { databankApi.getCharacterByName("Luke Skywalker") } returns Response.success(listOf(databankDto))
        every { dao.getCharacters() } returns flowOf(emptyList())

        // Act
        val result = repository.getCharacters()

        // Assert
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Luke Skywalker", result[0].name)
        assertEquals("image_url", result[0].imageUrl)
        coVerify { dao.insertCharacters(any()) }
    }

    @Test
    fun `getCharacters skips Databank API when imageUrl is already cached`() = runTest {
        // Arrange — DB has a character with a cached imageUrl; network is configured but must not be called
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val cachedEntity = createCharacterEntity(id = "1").copy(imageUrl = "cached_url", description = "cached_desc")

        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        every { dao.getCharacters() } returns flowOf(listOf(cachedEntity))

        // Act
        val result = repository.getCharacters()

        // Assert — returns DB data immediately; neither SWAPI nor Databank are called
        assertNotNull(result)
        assertEquals("cached_url", result!![0].imageUrl)
        coVerify(exactly = 0) { api.getCharacters() }
        coVerify(exactly = 0) { databankApi.getCharacterByName(any()) }
    }

    @Test
    fun `getCharacters returns error when network fails and DB is empty`() = runTest {
        // Arrange
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(emptyList())

        // Act
        val result = repository.getCharacters()

        // Assert
        assertNull(result)
    }

    // refreshCharactersFromNetwork() — always hits the network

    @Test
    fun `refreshCharactersFromNetwork always calls network even when cache is not empty`() = runTest {
        // Arrange
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val cachedEntity = createCharacterEntity(id = "1").copy(imageUrl = "cached_url", description = "cached_desc")
        val databankDto = DatabankCharacterDto(name = "Luke Skywalker", image = "cached_url", description = "cached_desc")

        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        coEvery { databankApi.getCharacterByName(any()) } returns Response.success(listOf(databankDto))
        every { dao.getCharacters() } returns flowOf(listOf(cachedEntity))

        // Act
        val result = repository.refreshCharactersFromNetwork()

        // Assert
        assertNotNull(result)
        coVerify(exactly = 1) { api.getCharacters() }
        coVerify { dao.insertCharacters(any()) }
    }

    @Test
    fun `refreshCharactersFromNetwork returns error when network fails`() = runTest {
        // Arrange
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(emptyList())

        // Act
        val result = repository.refreshCharactersFromNetwork()

        // Assert
        assertNull(result)
    }

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

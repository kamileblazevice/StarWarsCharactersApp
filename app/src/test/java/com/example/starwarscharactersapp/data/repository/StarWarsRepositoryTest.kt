package com.example.starwarscharactersapp.data.repository

import com.example.starwarscharactersapp.data.helper.ApiResult
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
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun `getCharacters returns success from API and saves to Dao`() = runTest {
        // Arrange
        val characterDto = StarWarsCharacterDto(name = "Luke Skywalker", url = "https://swapi.dev/api/people/1/")
        val databankDto = DatabankCharacterDto(name = "Luke Skywalker", image = "image_url", description = "desc")
        
        coEvery { api.getCharacters() } returns Response.success(listOf(characterDto))
        coEvery { databankApi.getCharacterByName("Luke Skywalker") } returns Response.success(listOf(databankDto))
        every { dao.getCharacters() } returns flowOf(emptyList())

        // Act
        val result = repository.getCharacters()

        // Assert
        assertTrue(result is ApiResult.Success)
        val characters = (result as ApiResult.Success).data
        assertEquals(1, characters.size)
        assertEquals("Luke Skywalker", characters[0].name)
        assertEquals("image_url", characters[0].imageUrl)
        
        coVerify { dao.insertCharacters(any()) }
    }

    @Test
    fun `getCharacters returns local data when API fails`() = runTest {
        // Arrange
        val localCharacter = createCharacterEntity(id = "1", name = "Luke Skywalker")
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(listOf(localCharacter))

        // Act
        val result = repository.getCharacters()

        // Assert
        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(1, data.size)
        assertEquals("Luke Skywalker", data[0].name)

        coVerify { api.getCharacters() }
        verify { dao.getCharacters() }
        coVerify(exactly = 0) { databankApi.getCharacterByName(any()) }
    }

    @Test
    fun `getCharacters returns error when API fails and local data is empty`() = runTest {
        // Arrange
        coEvery { api.getCharacters() } throws Exception("Network error")
        every { dao.getCharacters() } returns flowOf(emptyList())

        // Act
        val result = repository.getCharacters()

        // Assert
        assertTrue(result is ApiResult.Error)
        assertTrue((result as ApiResult.Error).message.contains("Network error"))
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
        vehicleUrls = emptyList()
    )
}

package com.example.starwarscharactersapp.data.model

import com.example.starwarscharactersapp.data.local.entity.CharacterEntity
import com.example.starwarscharactersapp.data.local.entity.FilmEntity
import com.example.starwarscharactersapp.data.local.entity.PlanetEntity
import com.example.starwarscharactersapp.data.local.entity.StarshipEntity
import com.example.starwarscharactersapp.data.local.entity.VehicleEntity
import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Test

class DtoMappersTest {

    @Test
    fun `StarWarsCharacterDto id is extracted from url`() {
        val dto = StarWarsCharacterDto(url = "https://swapi.dev/api/people/1/")

        assertEquals("1", dto.id)
    }

    @Test
    fun `StarWarsCharacterDto toCharacterEntity maps all fields`() {
        val dto = StarWarsCharacterDto(
            birthYear = "19BBY",
            eyeColor = "blue",
            filmUrls = listOf("https://swapi.dev/api/films/1/"),
            gender = "male",
            hairColor = "blond",
            height = "172",
            homeworld = "https://swapi.dev/api/planets/1/",
            mass = "77",
            name = "Luke Skywalker",
            skinColor = "fair",
            starshipUrls = listOf("https://swapi.dev/api/starships/12/"),
            vehicleUrls = listOf("https://swapi.dev/api/vehicles/14/"),
            url = "https://swapi.dev/api/people/1/",
            imageUrl = "image_url",
            description = "desc",
            isFavorite = true,
        )

        val entity = dto.toCharacterEntity()

        val expected = CharacterEntity(
            id = "1",
            url = "https://swapi.dev/api/people/1/",
            name = "Luke Skywalker",
            birthYear = "19BBY",
            eyeColor = "blue",
            gender = "male",
            hairColor = "blond",
            height = "172",
            homeworld = "https://swapi.dev/api/planets/1/",
            mass = "77",
            skinColor = "fair",
            filmUrls = listOf("https://swapi.dev/api/films/1/"),
            starshipUrls = listOf("https://swapi.dev/api/starships/12/"),
            vehicleUrls = listOf("https://swapi.dev/api/vehicles/14/"),
            imageUrl = "image_url",
            description = "desc",
            isFavorite = true,
        )
        assertEquals(expected, entity)
    }

    @Test
    fun `FilmDto id is extracted from url and releaseYear from releaseDate`() {
        val dto = FilmDto(
            title = "A New Hope",
            url = "https://swapi.dev/api/films/1/",
            director = "George Lucas",
            releaseDate = "1977-05-25",
        )

        assertEquals("1", dto.id)
        assertEquals("1977", dto.releaseYear)
    }

    @Test
    fun `FilmDto toFilm and toFilmEntity map all fields`() {
        val dto = FilmDto(
            title = "A New Hope",
            url = "https://swapi.dev/api/films/1/",
            director = "George Lucas",
            releaseDate = "1977-05-25",
        )

        val film = dto.toFilm()
        val entity = dto.toFilmEntity()

        val expectedFilm = Film(
            id = "1",
            title = "A New Hope",
            url = "https://swapi.dev/api/films/1/",
            director = "George Lucas",
            releaseYear = "1977",
        )
        val expectedEntity = FilmEntity(
            id = "1",
            url = "https://swapi.dev/api/films/1/",
            title = "A New Hope",
            director = "George Lucas",
            releaseYear = "1977",
        )
        assertEquals(expectedFilm, film)
        assertEquals(expectedEntity, entity)
    }

    @Test
    fun `PlanetDto id is extracted from url`() {
        val dto = PlanetDto(
            climate = "arid",
            diameter = "10465",
            gravity = "1 standard",
            name = "Tatooine",
            population = "200000",
            terrain = "desert",
            url = "https://swapi.dev/api/planets/1/",
        )

        assertEquals("1", dto.id)
    }

    @Test
    fun `PlanetDto toPlanet and toPlanetEntity map all fields`() {
        val dto = PlanetDto(
            climate = "arid",
            diameter = "10465",
            gravity = "1 standard",
            name = "Tatooine",
            population = "200000",
            terrain = "desert",
            url = "https://swapi.dev/api/planets/1/",
        )

        val planet = dto.toPlanet()
        val entity = dto.toPlanetEntity()

        val expectedPlanet = Planet(
            id = "1",
            name = "Tatooine",
            climate = "arid",
            terrain = "desert",
            population = "200000",
            gravity = "1 standard",
            diameter = "10465",
            url = "https://swapi.dev/api/planets/1/",
        )
        val expectedEntity = PlanetEntity(
            id = "1",
            url = "https://swapi.dev/api/planets/1/",
            name = "Tatooine",
            climate = "arid",
            diameter = "10465",
            gravity = "1 standard",
            population = "200000",
            terrain = "desert",
        )
        assertEquals(expectedPlanet, planet)
        assertEquals(expectedEntity, entity)
    }

    @Test
    fun `StarshipDto id, toStarship and toStarshipEntity map all fields`() {
        val dto = StarshipDto(name = "X-wing", url = "https://swapi.dev/api/starships/12/")

        val starship = dto.toStarship()
        val entity = dto.toStarshipEntity()

        val expectedStarship = Starship(id = "12", name = "X-wing", url = "https://swapi.dev/api/starships/12/")
        val expectedEntity = StarshipEntity(id = "12", url = "https://swapi.dev/api/starships/12/", name = "X-wing")

        assertEquals("12", dto.id)
        assertEquals(expectedStarship, starship)
        assertEquals(expectedEntity, entity)
    }

    @Test
    fun `VehicleDto id, toVehicle and toVehicleEntity map all fields`() {
        val dto = VehicleDto(name = "Snowspeeder", url = "https://swapi.dev/api/vehicles/14/")

        val vehicle = dto.toVehicle()
        val entity = dto.toVehicleEntity()

        val expectedVehicle = Vehicle(id = "14", name = "Snowspeeder", url = "https://swapi.dev/api/vehicles/14/")
        val expectedEntity = VehicleEntity(id = "14", url = "https://swapi.dev/api/vehicles/14/", name = "Snowspeeder")

        assertEquals("14", dto.id)
        assertEquals(expectedVehicle, vehicle)
        assertEquals(expectedEntity, entity)
    }

    @Test
    fun `DatabankCharacterDto defaults are empty strings`() {
        val dto = DatabankCharacterDto()

        val expected = DatabankCharacterDto(id = "", name = "", image = "", description = "")

        assertEquals(expected, dto)
    }
}
package com.example.starwarscharactersapp.data.local.entity

import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.domain.model.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EntityMappersTest {

    @Test
    fun `CharacterEntity toStarWarsCharacter maps all fields`() {
        val entity = CharacterEntity(
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

        val character = entity.toStarWarsCharacter()

        val expected = StarWarsCharacter(
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
        assertEquals(expected, character)
    }

    @Test
    fun `CharacterEntity toStarWarsCharacter maps null description to empty string`() {
        val entity = CharacterEntity(
            id = "1",
            url = "url",
            name = "name",
            birthYear = "",
            eyeColor = "",
            gender = "",
            hairColor = "",
            height = "",
            homeworld = "",
            mass = "",
            skinColor = "",
            filmUrls = emptyList(),
            starshipUrls = emptyList(),
            vehicleUrls = emptyList(),
            description = null,
        )

        val character = entity.toStarWarsCharacter()

        val expected = StarWarsCharacter(
            id = "1",
            url = "url",
            name = "name",
            description = "",
            isFavorite = false,
        )
        assertEquals(expected, character)
        assertFalse(character.isFavorite)
    }

    @Test
    fun `FilmEntity toFilm maps all fields`() {
        val entity = FilmEntity(
            id = "1",
            url = "url",
            title = "A New Hope",
            director = "George Lucas",
            releaseYear = "1977",
        )

        val film = entity.toFilm()

        val expected = Film(
            id = "1",
            url = "url",
            title = "A New Hope",
            director = "George Lucas",
            releaseYear = "1977",
        )
        assertEquals(expected, film)
    }

    @Test
    fun `PlanetEntity toPlanet maps all fields`() {
        val entity = PlanetEntity(
            id = "1",
            url = "url",
            name = "Tatooine",
            climate = "arid",
            diameter = "10465",
            gravity = "1 standard",
            population = "200000",
            terrain = "desert",
        )

        val planet = entity.toPlanet()

        val expected = Planet(
            id = "1",
            url = "url",
            name = "Tatooine",
            climate = "arid",
            diameter = "10465",
            gravity = "1 standard",
            population = "200000",
            terrain = "desert",
        )
        assertEquals(expected, planet)
    }

    @Test
    fun `StarshipEntity toStarship maps all fields`() {
        val entity = StarshipEntity(
            id = "12",
            url = "url",
            name = "X-wing",
        )

        val starship = entity.toStarship()

        val expected = Starship(
            id = "12",
            url = "url",
            name = "X-wing",
        )
        assertEquals(expected, starship)
    }

    @Test
    fun `VehicleEntity toVehicle maps all fields`() {
        val entity = VehicleEntity(
            id = "14",
            url = "url",
            name = "Snowspeeder",
        )

        val vehicle = entity.toVehicle()

        val expected = Vehicle(
            id = "14",
            url = "url",
            name = "Snowspeeder",
        )
        assertEquals(expected, vehicle)
    }
}
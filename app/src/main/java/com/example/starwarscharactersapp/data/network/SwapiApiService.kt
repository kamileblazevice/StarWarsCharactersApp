package com.example.starwarscharactersapp.data.network

import com.example.starwarscharactersapp.data.model.FilmDto
import com.example.starwarscharactersapp.data.model.PlanetDto
import com.example.starwarscharactersapp.data.model.StarWarsCharacterDto
import com.example.starwarscharactersapp.data.model.StarshipDto
import com.example.starwarscharactersapp.data.model.VehicleDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SwapiApiService {
    @GET("people")
    suspend fun getCharacters(): Response<List<StarWarsCharacterDto>>

    @GET("people/{id}")
    suspend fun getCharacter(@Path("id") id: String): Response<StarWarsCharacterDto>

    @GET("planets/{id}")
    suspend fun getPlanet(@Path("id") id: String): Response<PlanetDto>

    @GET("films/{id}")
    suspend fun getFilm(@Path("id") id: String): Response<FilmDto>

    @GET("starships/{id}")
    suspend fun getStarship(@Path("id") id: String): Response<StarshipDto>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleDto>
}


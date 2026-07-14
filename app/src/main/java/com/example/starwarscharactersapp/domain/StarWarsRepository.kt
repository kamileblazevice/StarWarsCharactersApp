package com.example.starwarscharactersapp.domain

import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface StarWarsRepository {
    suspend fun getCharacters(): List<StarWarsCharacter>?
    suspend fun refreshCharactersFromNetwork(): List<StarWarsCharacter>?
    suspend fun getCharacter(id: String): StarWarsCharacter?
    suspend fun toggleFavorite(id: String)
    fun getCharacterFlow(id: String): Flow<StarWarsCharacter?>
    fun getCharactersFlow(): Flow<List<StarWarsCharacter>>
    fun getFavoriteCharacters(): Flow<List<StarWarsCharacter>>
    suspend fun getPlanet(id: String): Planet?
    suspend fun getFilm(id: String): Film?
    suspend fun getStarship(id: String): Starship?
    suspend fun getVehicle(id: String): Vehicle?
    suspend fun syncAllData(): Boolean
}

package com.example.starwarscharactersapp.data.repository

import com.example.starwarscharactersapp.data.helper.ApiResult
import com.example.starwarscharactersapp.data.helper.safeApiCall
import com.example.starwarscharactersapp.data.local.StarWarsDao
import com.example.starwarscharactersapp.data.local.entity.toFilm
import com.example.starwarscharactersapp.data.local.entity.toPlanet
import com.example.starwarscharactersapp.data.local.entity.toStarWarsCharacter
import com.example.starwarscharactersapp.data.local.entity.toStarship
import com.example.starwarscharactersapp.data.local.entity.toVehicle
import com.example.starwarscharactersapp.data.model.DatabankCharacterDto
import com.example.starwarscharactersapp.data.model.StarWarsCharacterDto
import com.example.starwarscharactersapp.data.model.toCharacterEntity
import com.example.starwarscharactersapp.data.model.toFilm
import com.example.starwarscharactersapp.data.model.toFilmEntity
import com.example.starwarscharactersapp.data.model.toPlanet
import com.example.starwarscharactersapp.data.model.toPlanetEntity
import com.example.starwarscharactersapp.data.model.toStarship
import com.example.starwarscharactersapp.data.model.toStarshipEntity
import com.example.starwarscharactersapp.data.model.toVehicle
import com.example.starwarscharactersapp.data.model.toVehicleEntity
import com.example.starwarscharactersapp.data.network.DatabankApiService
import com.example.starwarscharactersapp.data.network.SwapiApiService
import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.Vehicle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StarWarsRepository @Inject constructor(
    private val api: SwapiApiService,
    private val databankApi: DatabankApiService,
    private val dao: StarWarsDao,
) {
    suspend fun getCharacters(): ApiResult<List<StarWarsCharacter>> = coroutineScope {
        val result = safeApiCall { api.getCharacters() }
        when (result) {
            is ApiResult.Success -> {
                val localCharacters = dao.getCharacters().first()
                val cachedById = localCharacters.associateBy { it.id }

                val enrichedDtos = result.data.map { dto ->
                    async {
                        val cached = cachedById[dto.id]
                        if (cached?.imageUrl != null) {
                            dto.copy(imageUrl = cached.imageUrl, description = cached.description)
                        } else {
                            enrichWithDatabankInfo(dto)
                        }
                    }
                }.awaitAll()

                val favoriteIds = localCharacters.filter { it.isFavorite }.map { it.id }.toSet()
                val entitiesToInsert = enrichedDtos.map { dto ->
                    dto.copy(isFavorite = favoriteIds.contains(dto.id)).toCharacterEntity()
                }
                dao.insertCharacters(entitiesToInsert)
                ApiResult.Success(entitiesToInsert.map { it.toStarWarsCharacter() })
            }

            is ApiResult.Error -> {
                val localCharacters = dao.getCharacters().first()
                if (localCharacters.isNotEmpty()) {
                    ApiResult.Success(localCharacters.map { it.toStarWarsCharacter() })
                } else {
                    result
                }
            }
        }
    }

    suspend fun getCharacter(id: String): ApiResult<StarWarsCharacter> {
        val result = safeApiCall { api.getCharacter(id) }
        return when (result) {
            is ApiResult.Success -> {
                val local = dao.getCharacterById(id)
                val dto = if (local?.imageUrl != null) {
                    result.data.copy(imageUrl = local.imageUrl, description = local.description)
                } else {
                    enrichWithDatabankInfo(result.data)
                }
                val charToInsert = dto.copy(isFavorite = local?.isFavorite ?: false).toCharacterEntity()
                dao.insertCharacters(listOf(charToInsert))
                ApiResult.Success(charToInsert.toStarWarsCharacter())
            }

            is ApiResult.Error -> {
                val local = dao.getCharacterById(id)
                if (local != null) {
                    ApiResult.Success(local.toStarWarsCharacter())
                } else {
                    result
                }
            }
        }
    }

    private suspend fun enrichWithDatabankInfo(dto: StarWarsCharacterDto): StarWarsCharacterDto {
        val info = getDatabankInfoByName(dto.name)
        return dto.copy(imageUrl = info?.image, description = info?.description)
    }

    private suspend fun getDatabankInfoByName(name: String): DatabankCharacterDto? {
        val result = safeApiCall { databankApi.getCharacterByName(name) }
        return (result as? ApiResult.Success)?.data?.firstOrNull()
    }

    suspend fun toggleFavorite(id: String) {
        dao.toggleFavoriteStatus(id)
    }

    fun getCharacterFlow(id: String): Flow<StarWarsCharacter?> {
        return dao.getCharacterFlowById(id).map { it?.toStarWarsCharacter() }
    }

    fun getCharactersFlow(): Flow<List<StarWarsCharacter>> {
        return dao.getCharacters().map { entities -> entities.map { it.toStarWarsCharacter() } }
    }

    fun getFavoriteCharacters(): Flow<List<StarWarsCharacter>> {
        return dao.getFavoriteCharacters().map { entities -> entities.map { it.toStarWarsCharacter() } }
    }

    suspend fun getPlanet(id: String): ApiResult<Planet> {
        val result = safeApiCall { api.getPlanet(id) }
        return when (result) {
            is ApiResult.Success -> {
                dao.insertPlanet(result.data.toPlanetEntity())
                ApiResult.Success(result.data.toPlanet())
            }
            is ApiResult.Error -> {
                val local = dao.getPlanetById(id)
                if (local != null) ApiResult.Success(local.toPlanet()) else result
            }
        }
    }

    suspend fun getFilm(id: String): ApiResult<Film> {
        val result = safeApiCall { api.getFilm(id) }
        return when (result) {
            is ApiResult.Success -> {
                dao.insertFilm(result.data.toFilmEntity())
                ApiResult.Success(result.data.toFilm())
            }
            is ApiResult.Error -> {
                val local = dao.getFilmById(id)
                if (local != null) ApiResult.Success(local.toFilm()) else result
            }
        }
    }

    suspend fun getStarship(id: String): ApiResult<Starship> {
        val result = safeApiCall { api.getStarship(id) }
        return when (result) {
            is ApiResult.Success -> {
                dao.insertStarship(result.data.toStarshipEntity())
                ApiResult.Success(result.data.toStarship())
            }
            is ApiResult.Error -> {
                val local = dao.getStarshipById(id)
                if (local != null) ApiResult.Success(local.toStarship()) else result
            }
        }
    }

    suspend fun getVehicle(id: String): ApiResult<Vehicle> {
        val result = safeApiCall { api.getVehicle(id) }
        return when (result) {
            is ApiResult.Success -> {
                dao.insertVehicle(result.data.toVehicleEntity())
                ApiResult.Success(result.data.toVehicle())
            }
            is ApiResult.Error -> {
                val local = dao.getVehicleById(id)
                if (local != null) ApiResult.Success(local.toVehicle()) else result
            }
        }
    }

    suspend fun syncAllData(): Boolean = coroutineScope {
        val charactersResult = getCharacters()
        if (charactersResult !is ApiResult.Success) return@coroutineScope false

        val characters = charactersResult.data
        val planetJobs = characters.map { it.homeworld.filter(Char::isDigit) }.distinct()
            .map { id -> async { getPlanet(id) } }
        val filmJobs = characters.flatMap { it.filmUrls }.map { it.filter(Char::isDigit) }.distinct()
            .map { id -> async { getFilm(id) } }
        val starshipJobs = characters.flatMap { it.starshipUrls }.map { it.filter(Char::isDigit) }.distinct()
            .map { id -> async { getStarship(id) } }
        val vehicleJobs = characters.flatMap { it.vehicleUrls }.map { it.filter(Char::isDigit) }.distinct()
            .map { id -> async { getVehicle(id) } }

        awaitAll(*planetJobs.toTypedArray(), *filmJobs.toTypedArray(), *starshipJobs.toTypedArray(), *vehicleJobs.toTypedArray())
        true
    }
}

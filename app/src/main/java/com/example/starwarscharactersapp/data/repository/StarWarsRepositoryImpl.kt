package com.example.starwarscharactersapp.data.repository

import android.util.Log
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
import com.example.starwarscharactersapp.domain.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.Film
import com.example.starwarscharactersapp.domain.model.Planet
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.domain.model.Starship
import com.example.starwarscharactersapp.domain.model.SyncProgress
import com.example.starwarscharactersapp.domain.model.Vehicle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

private const val TAG = "StarWarsRepository"
// Matches OkHttp's default per-host concurrency cap (Dispatcher.maxRequestsPerHost = 5);
private const val MAX_CONCURRENT_SYNC_REQUESTS = 5

class StarWarsRepositoryImpl @Inject constructor(
    private val api: SwapiApiService,
    private val databankApi: DatabankApiService,
    private val dao: StarWarsDao,
) : StarWarsRepository {
    private fun <T> ApiResult<T>.getOrNull(): T? = when (this) {
        is ApiResult.Success -> data
        is ApiResult.Error -> {
            Log.e(TAG, message, throwable)
            null
        }
    }

    override suspend fun getCharacters(): List<StarWarsCharacter>? {
        val local = dao.getCharacters().first()
        return if (local.isNotEmpty())
            local.map { it.toStarWarsCharacter() }
        else
            fetchAndCacheCharacters()
    }

    override suspend fun refreshCharactersFromNetwork(): List<StarWarsCharacter>? =
        fetchAndCacheCharacters()

    private suspend fun fetchAndCacheCharacters(): List<StarWarsCharacter>? = coroutineScope {
        safeApiCall { api.getCharacters() }.getOrNull()?.let { dtos ->
            val localCharacters = dao.getCharacters().first()
            val cachedById = localCharacters.associateBy { it.id }

            val enrichedDtos = dtos.map { dto ->
                async {
                    val cached = cachedById[dto.id]
                    if (cached?.imageUrl != null) {
                        dto.copy(imageUrl = cached.imageUrl, description = cached.description)
                    } else {
                        Semaphore(MAX_CONCURRENT_SYNC_REQUESTS).withPermit { enrichWithDatabankInfo(dto) }
                    }
                }
            }.awaitAll()

            val favoriteIds = localCharacters.filter { it.isFavorite }.map { it.id }.toSet()
            val entities = enrichedDtos.map { dto ->
                dto.copy(isFavorite = favoriteIds.contains(dto.id)).toCharacterEntity()
            }
            dao.insertCharacters(entities)
            entities.map { it.toStarWarsCharacter() }
        }
    }

    override suspend fun getCharacter(id: String): StarWarsCharacter? {
        val local = dao.getCharacterById(id)
        if (local != null) return local.toStarWarsCharacter()

        return safeApiCall { api.getCharacter(id) }.getOrNull()?.let { dto ->
            val enriched = enrichWithDatabankInfo(dto)
            val entity = enriched.copy(isFavorite = false).toCharacterEntity()
            dao.insertCharacters(listOf(entity))
            entity.toStarWarsCharacter()
        }
    }

    private suspend fun enrichWithDatabankInfo(dto: StarWarsCharacterDto): StarWarsCharacterDto {
        val info = getDatabankInfoByName(dto.name)
        return dto.copy(imageUrl = info?.image, description = info?.description)
    }

    private suspend fun getDatabankInfoByName(name: String): DatabankCharacterDto? =
        safeApiCall { databankApi.getCharacterByName(name) }.getOrNull()?.firstOrNull()

    override suspend fun toggleFavorite(id: String) {
        dao.toggleFavoriteStatus(id)
    }

    override fun getCharacterFlow(id: String): Flow<StarWarsCharacter?> {
        return dao.getCharacterFlowById(id).map { it?.toStarWarsCharacter() }
    }

    override fun getCharactersFlow(): Flow<List<StarWarsCharacter>> {
        return dao.getCharacters().map { entities -> entities.map { it.toStarWarsCharacter() } }
    }

    override fun getFavoriteCharacters(): Flow<List<StarWarsCharacter>> {
        return dao.getFavoriteCharacters().map { entities -> entities.map { it.toStarWarsCharacter() } }
    }

    override suspend fun getPlanet(id: String): Planet? {
        val local = dao.getPlanetById(id)
        if (local != null) return local.toPlanet()

        return safeApiCall { api.getPlanet(id) }.getOrNull()?.let { dto ->
            dao.insertPlanet(dto.toPlanetEntity())
            dto.toPlanet()
        }
    }

    override suspend fun getFilm(id: String): Film? {
        val local = dao.getFilmById(id)
        if (local != null) return local.toFilm()

        return safeApiCall { api.getFilm(id) }.getOrNull()?.let { dto ->
            dao.insertFilm(dto.toFilmEntity())
            dto.toFilm()
        }
    }

    override suspend fun getStarship(id: String): Starship? {
        val local = dao.getStarshipById(id)
        if (local != null) return local.toStarship()

        return safeApiCall { api.getStarship(id) }.getOrNull()?.let { dto ->
            dao.insertStarship(dto.toStarshipEntity())
            dto.toStarship()
        }
    }

    override suspend fun getVehicle(id: String): Vehicle? {
        val local = dao.getVehicleById(id)
        if (local != null) return local.toVehicle()

        return safeApiCall { api.getVehicle(id) }.getOrNull()?.let { dto ->
            dao.insertVehicle(dto.toVehicleEntity())
            dto.toVehicle()
        }
    }

    override fun syncAllData(): Flow<SyncProgress> = channelFlow {
        val characters = refreshCharactersFromNetwork()
        if (characters == null) {
            send(SyncProgress.Failure)
            return@channelFlow
        }

        val planetIds = characters.map { it.homeworld.filter(Char::isDigit) }.distinct()
        val filmIds = characters.flatMap { it.filmUrls }.map { it.filter(Char::isDigit) }.distinct()
        val starshipIds = characters.flatMap { it.starshipUrls }.map { it.filter(Char::isDigit) }.distinct()
        val vehicleIds = characters.flatMap { it.vehicleUrls }.map { it.filter(Char::isDigit) }.distinct()
        val total = planetIds.size + filmIds.size + starshipIds.size + vehicleIds.size

        val semaphore = Semaphore(MAX_CONCURRENT_SYNC_REQUESTS)
        val completedCount = AtomicInteger(0)
        val hasFailure = AtomicBoolean(false)

        suspend fun <T> fetchAndReportProgress(id: String, fetch: suspend (String) -> T?) {
            val result = semaphore.withPermit { fetch(id) }
            if (result == null) hasFailure.set(true)
            send(SyncProgress.InProgress(completedCount.incrementAndGet(), total))
        }

        val jobs =
            planetIds.map { id -> async { fetchAndReportProgress(id, ::getPlanet) } } +
                filmIds.map { id -> async { fetchAndReportProgress(id, ::getFilm) } } +
                starshipIds.map { id -> async { fetchAndReportProgress(id, ::getStarship) } } +
                vehicleIds.map { id -> async { fetchAndReportProgress(id, ::getVehicle) } }

        jobs.awaitAll()
        send(if (hasFailure.get()) SyncProgress.Failure else SyncProgress.Success)
    }
}

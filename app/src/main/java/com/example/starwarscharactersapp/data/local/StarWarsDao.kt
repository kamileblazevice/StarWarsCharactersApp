package com.example.starwarscharactersapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.starwarscharactersapp.data.local.entity.CharacterEntity
import com.example.starwarscharactersapp.data.local.entity.FilmEntity
import com.example.starwarscharactersapp.data.local.entity.PlanetEntity
import com.example.starwarscharactersapp.data.local.entity.StarshipEntity
import com.example.starwarscharactersapp.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StarWarsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Query("SELECT * FROM characters")
    fun getCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: String): CharacterEntity?

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterFlowById(id: String): Flow<CharacterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilm(film: FilmEntity)

    @Query("SELECT * FROM films WHERE id = :id")
    suspend fun getFilmById(id: String): FilmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanet(planet: PlanetEntity)

    @Query("SELECT * FROM planets WHERE id = :id")
    suspend fun getPlanetById(id: String): PlanetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStarship(starship: StarshipEntity)

    @Query("SELECT * FROM starships WHERE id = :id")
    suspend fun getStarshipById(id: String): StarshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): VehicleEntity?

    @Query("UPDATE characters SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavoriteStatus(id: String)

    @Query("SELECT * FROM characters WHERE isFavorite = 1")
    fun getFavoriteCharacters(): Flow<List<CharacterEntity>>

}

package com.example.starwarscharactersapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.starwarscharactersapp.data.local.entity.CharacterEntity
import com.example.starwarscharactersapp.data.local.entity.FilmEntity
import com.example.starwarscharactersapp.data.local.entity.PlanetEntity
import com.example.starwarscharactersapp.data.local.entity.StarshipEntity
import com.example.starwarscharactersapp.data.local.entity.VehicleEntity

@Database(
    entities = [
        CharacterEntity::class,
        FilmEntity::class,
        PlanetEntity::class,
        StarshipEntity::class,
        VehicleEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class StarWarsDatabase : RoomDatabase() {
    abstract val dao: StarWarsDao

    companion object {
        const val DATABASE_NAME = "star_wars_db"
    }
}

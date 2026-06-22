package com.example.starwarscharactersapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwarscharactersapp.domain.model.Planet

@Entity(tableName = "planets")
data class PlanetEntity(
    @PrimaryKey val id: String,
    val url: String,
    val name: String,
    val climate: String,
    val diameter: String,
    val gravity: String,
    val population: String,
    val terrain: String,
)

fun PlanetEntity.toPlanet(): Planet {
    return Planet(
        id = id,
        name = name,
        url = url,
        climate = climate,
        diameter = diameter,
        gravity = gravity,
        population = population,
        terrain = terrain,
    )
}

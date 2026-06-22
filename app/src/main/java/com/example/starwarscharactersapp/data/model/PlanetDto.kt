package com.example.starwarscharactersapp.data.model

import com.example.starwarscharactersapp.data.local.entity.PlanetEntity
import com.example.starwarscharactersapp.domain.model.Planet

data class PlanetDto(
    val climate: String,
    val diameter: String,
    val gravity: String,
    val name: String,
    val population: String,
    val terrain: String,
    val url: String,
) {
    val id: String get() = url.filter { it.isDigit() }
}

fun PlanetDto.toPlanet(): Planet {
    return Planet(
        id = id,
        name = name,
        climate = climate,
        terrain = terrain,
        population = population,
        gravity = gravity,
        diameter = diameter,
        url = url,
    )
}

fun PlanetDto.toPlanetEntity(): PlanetEntity {
    return PlanetEntity(
        id = id,
        url = url,
        name = name,
        climate = climate,
        terrain = terrain,
        population = population,
        gravity = gravity,
        diameter = diameter,
    )
}

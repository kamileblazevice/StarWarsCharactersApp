package com.example.starwarscharactersapp.data.model

import com.example.starwarscharactersapp.data.local.entity.StarshipEntity
import com.example.starwarscharactersapp.domain.model.Starship

data class StarshipDto(
    val name: String,
    val url: String,
) {
    val id: String get() = url.filter { it.isDigit() }
}

fun StarshipDto.toStarship(): Starship {
    return Starship(
        id = id,
        name = name,
        url = url,
    )
}

fun StarshipDto.toStarshipEntity(): StarshipEntity {
    return StarshipEntity(
        id = id,
        url = url,
        name = name,
    )
}

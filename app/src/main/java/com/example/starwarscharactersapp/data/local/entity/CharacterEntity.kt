package com.example.starwarscharactersapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val url: String,
    val name: String,
    val birthYear: String,
    val eyeColor: String,
    val gender: String,
    val hairColor: String,
    val height: String,
    val homeworld: String,
    val mass: String,
    val skinColor: String,
    val filmUrls: List<String>,
    val starshipUrls: List<String>,
    val vehicleUrls: List<String>,
    val imageUrl: String? = null,
    val description: String? = null,
    val isFavorite: Boolean = false,
)

fun CharacterEntity.toStarWarsCharacter(): StarWarsCharacter {
    return StarWarsCharacter(
        id = id,
        name = name,
        url = url,
        birthYear = birthYear,
        eyeColor = eyeColor,
        gender = gender,
        hairColor = hairColor,
        height = height,
        homeworld = homeworld,
        mass = mass,
        skinColor = skinColor,
        filmUrls = filmUrls,
        starshipUrls = starshipUrls,
        vehicleUrls = vehicleUrls,
        imageUrl = imageUrl,
        description = description ?: "",
        isFavorite = isFavorite,
    )
}

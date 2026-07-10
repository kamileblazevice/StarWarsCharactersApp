package com.example.starwarscharactersapp.data.model

import com.example.starwarscharactersapp.data.local.entity.CharacterEntity
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class StarWarsCharacterDto(
    @SerializedName("birth_year")
    val birthYear: String = "",
    @SerializedName("eye_color")
    val eyeColor: String = "",
    @SerializedName("films")
    val filmUrls: List<String> = emptyList(),
    val gender: String = "",
    @SerializedName("hair_color")
    val hairColor: String = "",
    val height: String = "",
    val homeworld: String = "",
    val mass: String = "",
    val name: String = "",
    @SerializedName("skin_color")
    val skinColor: String = "",
    @SerializedName("starships")
    val starshipUrls: List<String> = emptyList(),
    @SerializedName("vehicles")
    val vehicleUrls: List<String> = emptyList(),
    val url: String = "",
    val imageUrl: String? = null,
    val description: String? = null,
    val isFavorite: Boolean = false,
) {
    val id: String get() = url.filter { it.isDigit() }
}

fun StarWarsCharacterDto.toCharacterEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
        url = url,
        name = name,
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
        description = description,
        isFavorite = isFavorite
    )
}

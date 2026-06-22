package com.example.starwarscharactersapp.data.model

import com.example.starwarscharactersapp.data.local.entity.FilmEntity
import com.example.starwarscharactersapp.domain.model.Film
import com.google.gson.annotations.SerializedName

data class FilmDto(
    val title: String,
    val url: String,
    val director: String,
    @SerializedName("release_date")
    val releaseDate: String,
) {
    val id: String get() = url.filter { it.isDigit() }
    val releaseYear: String get() = releaseDate.take(4)
}

fun FilmDto.toFilm(): Film {
    return Film(
        id = id,
        title = title,
        url = url,
        director = director,
        releaseYear = releaseYear,
    )
}

fun FilmDto.toFilmEntity(): FilmEntity {
    return FilmEntity(
        id = id,
        url = url,
        title = title,
        director = director,
        releaseYear = releaseYear,
    )
}

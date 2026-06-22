package com.example.starwarscharactersapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwarscharactersapp.domain.model.Film

@Entity(tableName = "films")
data class FilmEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val director: String,
    val releaseYear: String,
)

fun FilmEntity.toFilm(): Film {
    return Film(
        id = id,
        title = title,
        url = url,
        director = director,
        releaseYear = releaseYear,
    )
}

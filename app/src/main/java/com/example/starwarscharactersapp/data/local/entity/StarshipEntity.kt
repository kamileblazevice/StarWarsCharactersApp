package com.example.starwarscharactersapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwarscharactersapp.domain.model.Starship

@Entity(tableName = "starships")
data class StarshipEntity(
    @PrimaryKey val id: String,
    val url: String,
    val name: String,
)

fun StarshipEntity.toStarship(): Starship {
    return Starship(
        id = id,
        name = name,
        url = url,
    )
}

package com.example.starwarscharactersapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object CharactersList : Route

    @Serializable
    data class CharacterDetail(val characterId: String) : Route

    @Serializable
    data object Favorites : Route


    @Serializable
    data object Settings : Route
}


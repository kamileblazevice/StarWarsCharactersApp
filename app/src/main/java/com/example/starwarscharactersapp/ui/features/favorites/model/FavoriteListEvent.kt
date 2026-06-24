package com.example.starwarscharactersapp.ui.features.favorites.model

sealed class FavoriteListEvent {
    data class OnToggleFavorite(val characterId: String) : FavoriteListEvent()
}

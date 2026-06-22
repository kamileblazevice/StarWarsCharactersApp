package com.example.starwarscharactersapp.ui.features.detail.model

sealed class CharacterDetailEvent {
    data object OnRetryPlanet : CharacterDetailEvent()
    data object OnRetryFilms : CharacterDetailEvent()
    data object OnRetryStarships : CharacterDetailEvent()
    data object OnRetryVehicles : CharacterDetailEvent()
    data class OnImageError(val error: String?) : CharacterDetailEvent()
    data object OnReloadData : CharacterDetailEvent()
    data object OnToggleFavorite : CharacterDetailEvent()
}
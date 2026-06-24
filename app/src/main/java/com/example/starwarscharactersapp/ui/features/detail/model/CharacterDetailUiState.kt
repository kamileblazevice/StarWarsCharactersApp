package com.example.starwarscharactersapp.ui.features.detail.model

import com.example.starwarscharactersapp.domain.model.StarWarsCharacter

data class CharacterDetailUiState(
    val character: StarWarsCharacter,
    val planetError: Boolean = false,
    val filmsError: Boolean = false,
    val starshipsError: Boolean = false,
    val vehiclesError: Boolean = false,
    val imageReloadKey: Int = 0,
)
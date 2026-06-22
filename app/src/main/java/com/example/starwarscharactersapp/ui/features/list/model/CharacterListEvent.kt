package com.example.starwarscharactersapp.ui.features.list.model

sealed class CharacterListEvent {
    data class OnSearchQueryChanged(val query: String) : CharacterListEvent()
    data object OnReloadData : CharacterListEvent()
    data class OnToggleFavorite(val characterId: String) : CharacterListEvent()
    data class OnImageErrorChange(val characterId: String, val error: String?) :
        CharacterListEvent()
}

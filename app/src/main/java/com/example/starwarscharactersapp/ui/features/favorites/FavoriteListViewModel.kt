package com.example.starwarscharactersapp.ui.features.favorites

import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.favorites.model.FavoriteListEvent
import com.example.starwarscharactersapp.ui.helper.BaseViewModel
import com.example.starwarscharactersapp.ui.helper.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteListViewModel @Inject constructor(
    private val repository: StarWarsRepository,
    private val networkMonitor: NetworkMonitor,
) : BaseViewModel<FavoriteListEvent>() {

    private val _state = MutableStateFlow<UiState<List<StarWarsCharacter>>>(UiState.Loading)
    val state: StateFlow<UiState<List<StarWarsCharacter>>> = _state.asStateFlow()

    private val _imageReloadRevision = MutableStateFlow(0)
    val imageReloadRevision: StateFlow<Int> = _imageReloadRevision.asStateFlow()

    init {
        observeFavorites()
        observeNetwork()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteCharacters().collect { favorites ->
                _state.value = UiState.Success(favorites.sortedBy { it.name })
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect {
                _imageReloadRevision.update { it + 1 }
            }
        }
    }

    override fun onEvent(event: FavoriteListEvent) {
        when (event) {
            is FavoriteListEvent.OnToggleFavorite -> toggleFavorite(event.characterId)
        }
    }

    private fun toggleFavorite(characterId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(characterId)
        }
    }
}

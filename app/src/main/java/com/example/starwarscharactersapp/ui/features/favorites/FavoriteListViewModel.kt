package com.example.starwarscharactersapp.ui.features.favorites

import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.helper.ApiResult
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
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    if (_state.value is UiState.Success) {
                        _state.update { currentState ->

                            if (currentState is UiState.Success) {

                                val updatedList = currentState.data.map {
                                    if (it.imageError != null && it.description.isEmpty()) {
                                        when (val result = repository.getCharacter(it.id)) {
                                            is ApiResult.Success -> {
                                                val character = result.data

                                                it.copy(
                                                    imageUrl = character.imageUrl,
                                                    description = character.description,
                                                    imageRetryKey = it.imageRetryKey + 1
                                                )
                                            }
                                            else -> {
                                                it
                                            }
                                        }

                                    } else if (it.imageError != null && !it.imageUrl.isNullOrEmpty()) {
                                        it.copy(imageRetryKey = it.imageRetryKey + 1)
                                    } else {
                                        it
                                    }
                                }
                                UiState.Success(updatedList)

                            } else currentState
                        }
                    }
                }
            }
        }
    }

    override fun onEvent(event: FavoriteListEvent) {
        when (event) {
            is FavoriteListEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(event.characterId)
                }
            }

            is FavoriteListEvent.OnImageErrorChange -> {
                _state.update { currentState ->
                    if (currentState is UiState.Success) {
                        val updatedList = currentState.data.map {
                            if (it.id == event.characterId) {
                                it.copy(
                                    imageError = event.error,
                                    imageRetryKey = if (event.error != null) it.imageRetryKey + 1 else it.imageRetryKey)
                            } else {
                                it
                            }
                        }
                        UiState.Success(updatedList)
                    } else currentState
                }
            }
        }
    }
}

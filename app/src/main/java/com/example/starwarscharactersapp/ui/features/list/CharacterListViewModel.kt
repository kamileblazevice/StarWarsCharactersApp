package com.example.starwarscharactersapp.ui.features.list

import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.helper.ApiResult
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.list.model.CharacterListEvent
import com.example.starwarscharactersapp.ui.helper.BaseViewModel
import com.example.starwarscharactersapp.ui.helper.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val repository: StarWarsRepository,
    private val networkMonitor: NetworkMonitor,
) : BaseViewModel<CharacterListEvent>() {

    private val _state = MutableStateFlow<UiState<List<StarWarsCharacter>>>(UiState.Loading)
    val state: StateFlow<UiState<List<StarWarsCharacter>>> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeCharacters()
        refreshCharacters()
        observeNetwork()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    reloadDataIfNeeded()
                }
            }
        }
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            repository.getCharactersFlow()
                .flowOn(Dispatchers.Default)
                .combine(searchQuery.debounce(300)) { characters, query ->
                    val filtered = if (query.isEmpty()) {
                        characters
                    } else {
                        characters.filter { it.name.contains(query, ignoreCase = true) }
                    }
                    filtered.sortedBy { it.name }
                }
                .collect { filteredCharacters ->
                    if (filteredCharacters.isNotEmpty() || (_state.value is UiState.Success)) {
                        _state.value = UiState.Success(filteredCharacters)
                    }
                }
        }
    }

    private fun refreshCharacters() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = repository.getCharacters()
            if (result is ApiResult.Error && _state.value is UiState.Loading) {
                delay(2000)
                _state.value = UiState.Error(result.message)
            }
        }
    }

    private fun reloadDataIfNeeded() {
        viewModelScope.launch {
            if (_state.value is UiState.Error) {
                refreshCharacters()
            } else if (_state.value is UiState.Success) {
                _state.update { currentState ->
                    if (currentState is UiState.Success) {
                        if (currentState.data.any { it.imageError != null && it.description.isEmpty() }) {
                            repository.getCharacters()
                        }
                        val updatedList = currentState.data.map {
                            if (it.imageError != null) {
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

    override fun onEvent(event: CharacterListEvent) {
        when (event) {
            is CharacterListEvent.OnImageErrorChange -> {
                _state.update { currentState ->
                    if (currentState is UiState.Success) {
                        val updatedList = currentState.data.map {
                            if (it.id == event.characterId) {
                                it.copy(imageError = event.error)
                            } else {
                                it
                            }
                        }
                        UiState.Success(updatedList)
                    } else currentState
                }
            }

            CharacterListEvent.OnReloadData -> refreshCharacters()

            is CharacterListEvent.OnSearchQueryChanged -> {
                _searchQuery.value = event.query
            }

            is CharacterListEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(event.characterId)
                }
            }
        }
    }
}

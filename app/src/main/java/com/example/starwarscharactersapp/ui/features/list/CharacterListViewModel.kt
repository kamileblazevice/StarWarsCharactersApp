package com.example.starwarscharactersapp.ui.features.list

import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.list.model.CharacterListEvent
import com.example.starwarscharactersapp.ui.helper.BaseViewModel
import com.example.starwarscharactersapp.ui.helper.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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

    private val _imageReloadRevision = MutableStateFlow(0)
    val imageReloadRevision: StateFlow<Int> = _imageReloadRevision.asStateFlow()

    init {
        observeCharacters()
        loadInitialData()
        observeNetwork()
    }

    private fun observeCharacters() {
        viewModelScope.launch {
            repository.getCharactersFlow()
                .combine(_searchQuery.debounce(300)) { characters, query ->
                    val filtered = if (query.isEmpty()) characters
                    else characters.filter { it.name.contains(query, ignoreCase = true) }
                    filtered.sortedBy { it.name }
                }
                .collect { filtered ->
                    if (filtered.isNotEmpty() || _state.value is UiState.Success) {
                        _state.value = UiState.Success(filtered)
                    }
                }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val characters = repository.getCharacters()
            if (characters != null) {
                if (_state.value is UiState.Loading)
                    _state.value = UiState.Success(characters.sortedBy { it.name })
            } else {
                if (_state.value !is UiState.Success)
                    delay(1000)
                    _state.value = UiState.Error("Failed to load characters")
            }
        }
    }

    private fun reloadFromNetwork() {
        viewModelScope.launch {
            if (_state.value !is UiState.Success) _state.value = UiState.Loading
            val characters = repository.refreshCharactersFromNetwork()
            if (characters == null && _state.value !is UiState.Success)
                delay(1000)
                _state.value = UiState.Error("Failed to load characters")
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect {
                if (_state.value is UiState.Error) {
                    reloadFromNetwork()
                } else {
                    _imageReloadRevision.update { it + 1 }
                }
            }
        }
    }

    override fun onEvent(event: CharacterListEvent) {
        when (event) {
            is CharacterListEvent.OnSearchQueryChanged -> changeSearchQuery(event.query)
            CharacterListEvent.OnReloadData -> reloadFromNetwork()
            is CharacterListEvent.OnToggleFavorite -> toggleFavorite(event.characterId)
        }
    }

    private fun changeSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun toggleFavorite(characterId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(characterId)
        }
    }
}

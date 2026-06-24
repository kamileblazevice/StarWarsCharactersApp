package com.example.starwarscharactersapp.ui.features.detail

import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.helper.ApiResult
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.ui.features.detail.model.CharacterDetailEvent
import com.example.starwarscharactersapp.ui.features.detail.model.CharacterDetailUiState
import com.example.starwarscharactersapp.ui.helper.BaseViewModel
import com.example.starwarscharactersapp.ui.helper.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CharacterDetailViewModel.Factory::class)
class CharacterDetailViewModel @AssistedInject constructor(
    private val repository: StarWarsRepository,
    private val networkMonitor: NetworkMonitor,
    @Assisted private val characterId: String,
) : BaseViewModel<CharacterDetailEvent>() {

    @AssistedFactory
    interface Factory {
        fun create(characterId: String): CharacterDetailViewModel
    }

    private val _state = MutableStateFlow<UiState<CharacterDetailUiState>>(UiState.Loading)
    val state = _state.asStateFlow()

    init {
        loadCharacter()
        observeCharacter()
        observeNetwork()
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = repository.getCharacter(characterId)) {
                is ApiResult.Success -> {
                    val character = result.data
                    _state.value = UiState.Success(CharacterDetailUiState(character = character))
                    if (character.homeworld.isNotEmpty()) loadPlanet(character.homeworld)
                    if (character.filmUrls.isNotEmpty()) loadFilms(character.filmUrls)
                    if (character.starshipUrls.isNotEmpty()) loadStarships(character.starshipUrls)
                    if (character.vehicleUrls.isNotEmpty()) loadVehicles(character.vehicleUrls)
                }
                is ApiResult.Error -> _state.value = UiState.Error(result.message)
            }
        }
    }

    private fun observeCharacter() {
        viewModelScope.launch {
            repository.getCharacterFlow(characterId).collect { character ->
                character?.let {
                    _state.update { currentState ->
                        if (currentState is UiState.Success) {
                            UiState.Success(currentState.data.copy(character = currentState.data.character.copy(isFavorite = it.isFavorite)))
                        } else currentState
                    }
                }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.filter { it }.collect { retryAllFailed() }
        }
    }

    private fun retryAllFailed() {
        val currentState = _state.value
        if (currentState is UiState.Error) {
            loadCharacter()
            return
        }
        if (currentState !is UiState.Success) return
        val uiState = currentState.data
        _state.update { if (it is UiState.Success) UiState.Success(it.data.copy(imageReloadKey = it.data.imageReloadKey + 1)) else it }
        if (uiState.planetError) loadPlanet(uiState.character.homeworld)
        if (uiState.filmsError) loadFilms(uiState.character.filmUrls)
        if (uiState.starshipsError) loadStarships(uiState.character.starshipUrls)
        if (uiState.vehiclesError) loadVehicles(uiState.character.vehicleUrls)
    }

    private fun loadPlanet(url: String) {
        val id = url.filter { it.isDigit() }
        viewModelScope.launch {
            _state.update { if (it is UiState.Success) UiState.Success(it.data.copy(planetError = false)) else it }
            when (val result = repository.getPlanet(id)) {
                is ApiResult.Success -> _state.update { currentState ->
                    if (currentState is UiState.Success) {
                        UiState.Success(currentState.data.copy(character = currentState.data.character.copy(planet = result.data)))
                    } else currentState
                }
                is ApiResult.Error -> _state.update { if (it is UiState.Success) UiState.Success(it.data.copy(planetError = true)) else it }
            }
        }
    }

    private fun loadFilms(filmUrls: List<String>) {
        viewModelScope.launch {
            _state.update { if (it is UiState.Success) UiState.Success(it.data.copy(filmsError = false)) else it }
            val results = filmUrls.map { url ->
                async { repository.getFilm(url.filter { c -> c.isDigit() }) }
            }.awaitAll()
            val fetchedFilms = results.mapNotNull { (it as? ApiResult.Success)?.data }
            val hasError = results.any { it is ApiResult.Error }
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(
                        currentState.data.copy(
                            character = currentState.data.character.copy(films = fetchedFilms),
                            filmsError = hasError && fetchedFilms.isEmpty(),
                        )
                    )
                } else currentState
            }
        }
    }

    private fun loadStarships(starshipUrls: List<String>) {
        viewModelScope.launch {
            _state.update { if (it is UiState.Success) UiState.Success(it.data.copy(starshipsError = false)) else it }
            val results = starshipUrls.map { url ->
                async { repository.getStarship(url.filter { c -> c.isDigit() }) }
            }.awaitAll()
            val fetchedStarships = results.mapNotNull { (it as? ApiResult.Success)?.data }
            val hasError = results.any { it is ApiResult.Error }
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(
                        currentState.data.copy(
                            character = currentState.data.character.copy(starships = fetchedStarships),
                            starshipsError = hasError && fetchedStarships.isEmpty(),
                        )
                    )
                } else currentState
            }
        }
    }

    private fun loadVehicles(vehicleUrls: List<String>) {
        viewModelScope.launch {
            _state.update { if (it is UiState.Success) UiState.Success(it.data.copy(vehiclesError = false)) else it }
            val results = vehicleUrls.map { url ->
                async { repository.getVehicle(url.filter { c -> c.isDigit() }) }
            }.awaitAll()
            val fetchedVehicles = results.mapNotNull { (it as? ApiResult.Success)?.data }
            val hasError = results.any { it is ApiResult.Error }
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(
                        currentState.data.copy(
                            character = currentState.data.character.copy(vehicles = fetchedVehicles),
                            vehiclesError = hasError && fetchedVehicles.isEmpty(),
                        )
                    )
                } else currentState
            }
        }
    }

    override fun onEvent(event: CharacterDetailEvent) {
        when (event) {
            CharacterDetailEvent.OnReloadData -> loadCharacter()
            CharacterDetailEvent.OnToggleFavorite -> toggleFavorite()
            CharacterDetailEvent.OnRetryPlanet -> retryPlanet()
            CharacterDetailEvent.OnRetryFilms -> retryFilms()
            CharacterDetailEvent.OnRetryStarships -> retryStarships()
            CharacterDetailEvent.OnRetryVehicles -> retryVehicles()
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(characterId)
        }
    }

    private fun retryPlanet() {
        (_state.value as? UiState.Success)?.data?.let { loadPlanet(it.character.homeworld) }
    }

    private fun retryFilms() {
        (_state.value as? UiState.Success)?.data?.let { loadFilms(it.character.filmUrls) }
    }

    private fun retryStarships() {
        (_state.value as? UiState.Success)?.data?.let { loadStarships(it.character.starshipUrls) }
    }

    private fun retryVehicles() {
        (_state.value as? UiState.Success)?.data?.let { loadVehicles(it.character.vehicleUrls) }
    }
}

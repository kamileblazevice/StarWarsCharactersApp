package com.example.starwarscharactersapp.ui.features.detail

import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.helper.ApiResult
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.StarWarsCharacter
import com.example.starwarscharactersapp.ui.features.detail.model.CharacterDetailEvent
import com.example.starwarscharactersapp.ui.helper.BaseViewModel
import com.example.starwarscharactersapp.ui.helper.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _state = MutableStateFlow<UiState<StarWarsCharacter>>(UiState.Loading)
    val state = _state.asStateFlow()

    private val _reloadKey = MutableStateFlow(0)
    val reloadKey = _reloadKey.asStateFlow()

    init {
        observeCharacter()
        getCharacterDetail()
        observeNetwork()
    }

    private fun observeCharacter() {
        viewModelScope.launch {
            repository.getCharacterFlow(characterId).collect { character ->
                character?.let { char ->
                    _state.update { currentState ->
                        if (currentState is UiState.Success) {
                            UiState.Success(
                                currentState.data.copy(
                                    isFavorite = char.isFavorite,
                                )
                            )
                        } else {
                            currentState
                        }
                    }
                }
            }
        }
    }


    fun getCharacterDetail() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = repository.getCharacter(characterId)) {
                is ApiResult.Success -> {
                    val character = result.data
                    _state.value = UiState.Success(character)
                    if (character.homeworld.isNotEmpty()) {
                        getPlanet(character.homeworld)
                    }
                    if (character.filmUrls.isNotEmpty()) {
                        getFilms(character.filmUrls)
                    }
                    if (character.starshipUrls.isNotEmpty()) {
                        getStarships(character.starshipUrls)
                    }
                    if (character.vehicleUrls.isNotEmpty()) {
                        getVehicles(character.vehicleUrls)
                    }
                }

                is ApiResult.Error -> {
                    _state.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun getPlanet(url: String) {
        val id = url.filter { it.isDigit() }
        viewModelScope.launch {
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(currentState.data.copy(planetError = null))
                } else currentState
            }

            when (val result = repository.getPlanet(id)) {
                is ApiResult.Success -> {
                    _state.update { currentState ->
                        if (currentState is UiState.Success) {
                            UiState.Success(
                                currentState.data.copy(
                                    planet = result.data, planetError = null
                                )
                            )
                        } else {
                            currentState
                        }
                    }
                }

                is ApiResult.Error -> {
                    delay(1000)
                    _state.update { currentState ->
                        if (currentState is UiState.Success) {
                            UiState.Success(currentState.data.copy(planetError = result.message))
                        } else currentState
                    }
                }
            }
        }
    }

    fun getFilms(filmUrls: List<String>) {
        viewModelScope.launch {
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(currentState.data.copy(filmsError = null))
                } else currentState
            }

            val results = filmUrls.map { url ->
                val id = url.filter { it.isDigit() }
                async { repository.getFilm(id) }
            }.awaitAll()

            val fetchedFilms = results.mapNotNull { (it as? ApiResult.Success)?.data }
            val firstError = results.filterIsInstance<ApiResult.Error>().firstOrNull()
            delay(1000)

            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(
                        currentState.data.copy(
                            films = fetchedFilms, filmsError = firstError?.message
                        )
                    )
                } else currentState
            }
        }
    }

    fun getStarships(starshipUrls: List<String>) {
        viewModelScope.launch {
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(currentState.data.copy(starshipsError = null))
                } else currentState
            }

            val results = starshipUrls.map { url ->
                val id = url.filter { it.isDigit() }
                async { repository.getStarship(id) }
            }.awaitAll()
            delay(1000)

            val fetchedStarships = results.mapNotNull { (it as? ApiResult.Success)?.data }
            val firstError = results.filterIsInstance<ApiResult.Error>().firstOrNull()

            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(
                        currentState.data.copy(
                            starships = fetchedStarships, starshipsError = firstError?.message
                        )
                    )
                } else currentState
            }
        }
    }

    fun getVehicles(vehicleUrls: List<String>) {
        viewModelScope.launch {
            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(currentState.data.copy(vehiclesError = null))
                } else currentState
            }

            val results = vehicleUrls.map { url ->
                val id = url.filter { it.isDigit() }
                async { repository.getVehicle(id) }
            }.awaitAll()
            delay(1000)

            val fetchedVehicles = results.mapNotNull { (it as? ApiResult.Success)?.data }
            val firstError = results.filterIsInstance<ApiResult.Error>().firstOrNull()

            _state.update { currentState ->
                if (currentState is UiState.Success) {
                    UiState.Success(
                        currentState.data.copy(
                            vehicles = fetchedVehicles, vehiclesError = firstError?.message
                        )
                    )
                } else currentState
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    retryAllFailed()
                }
            }
        }
    }

    private fun retryAllFailed() {
        val currentState = _state.value
        if (currentState is UiState.Error) {
            getCharacterDetail()
        } else if (currentState is UiState.Success) {
            val character = (_state.value as? UiState.Success)?.data ?: return
            if (character.imageUrl?.isNotEmpty() == true) {
                refreshImage()
            } else {
                viewModelScope.launch {
                    when (val result = repository.getCharacter(characterId)) {
                        is ApiResult.Success -> {
                            val character = result.data
                            _state.update { currentState ->
                                if (currentState is UiState.Success) {
                                    UiState.Success(currentState.data.copy(imageUrl = character.imageUrl))
                                } else currentState
                            }
                            refreshImage()
                        }

                        else -> {}
                    }
                }
            }
            if (character.planetError != null) {
                getPlanet(character.homeworld)
            }
            if (character.filmsError != null) {
                getFilms(character.filmUrls)
            }
            if (character.starshipsError != null) {
                getStarships(character.starshipUrls)
            }
            if (character.vehiclesError != null) {
                getVehicles(character.vehicleUrls)
            }
        }
    }

    fun refreshImage() {
        _reloadKey.update { it + 1 }
    }


    override fun onEvent(event: CharacterDetailEvent) {
        when (event) {
            is CharacterDetailEvent.OnReloadData -> {
                getCharacterDetail()
            }

            CharacterDetailEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(characterId)
                }
            }

            CharacterDetailEvent.OnRetryFilms -> {
                (_state.value as? UiState.Success)?.data?.let { character ->
                    getFilms(character.filmUrls)
                }
            }

            CharacterDetailEvent.OnRetryPlanet -> {
                (_state.value as? UiState.Success)?.data?.let { character ->
                    getPlanet(character.homeworld)
                }
            }

            CharacterDetailEvent.OnRetryStarships -> {
                (_state.value as? UiState.Success)?.data?.let { character ->
                    getStarships(character.starshipUrls)
                }
            }

            CharacterDetailEvent.OnRetryVehicles -> {
                (_state.value as? UiState.Success)?.data?.let { character ->
                    getVehicles(character.vehicleUrls)
                }
            }

            is CharacterDetailEvent.OnImageError -> {
                _state.update { currentState ->
                    if (currentState is UiState.Success) {
                        UiState.Success(currentState.data.copy(imageError = event.error))
                    } else currentState
                }
            }
        }
    }
}

package com.example.starwarscharactersapp.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: StarWarsRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded = _isDataLoaded.asStateFlow()

    init {
        checkAndSyncData()
    }

    private fun checkAndSyncData() {
        viewModelScope.launch {
            val lastSync = prefsManager.lastSyncTimestamp.first()
            val currentTime = System.currentTimeMillis()
            val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L

            if (currentTime - lastSync > oneWeekInMillis) {
                val success = repository.syncAllData()
                if (success) {
                    prefsManager.updateSyncTimestamp(currentTime)
                }
            }
            _isDataLoaded.update { true }
        }
    }
}

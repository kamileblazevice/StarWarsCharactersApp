package com.example.starwarscharactersapp.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.domain.StarWarsRepository
import com.example.starwarscharactersapp.domain.model.SyncProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_SPLASH_DURATION_MS = 2000L

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: StarWarsRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded = _isDataLoaded.asStateFlow()

    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    val syncProgress = _syncProgress.asStateFlow()

    init {
        checkAndSyncData()
    }

    private fun checkAndSyncData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val lastSync = prefsManager.lastSyncTimestamp.first()
            val currentTime = System.currentTimeMillis()
            val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L

            if (currentTime - lastSync > oneWeekInMillis) {
                repository.syncAllData().collect { progress ->
                    _syncProgress.value = progress
                    if (progress is SyncProgress.Success) {
                        prefsManager.updateSyncTimestamp(currentTime)
                    }
                }
            }

            // If sync was skipped or failed fast, the splash would be shown for MIN_SPLASH_DURATION_MS
            val remainingSplashTime = MIN_SPLASH_DURATION_MS - (System.currentTimeMillis() - startTime)
            if (remainingSplashTime > 0) delay(remainingSplashTime)

            _isDataLoaded.update { true }
        }
    }
}

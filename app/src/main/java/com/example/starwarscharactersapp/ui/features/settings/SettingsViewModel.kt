package com.example.starwarscharactersapp.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starwarscharactersapp.data.local.PrefsManager
import com.example.starwarscharactersapp.data.local.ThemeMode
import com.example.starwarscharactersapp.ui.features.list.model.CharacterListEvent
import com.example.starwarscharactersapp.ui.features.settings.model.SettingsEvent
import com.example.starwarscharactersapp.ui.helper.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: PrefsManager
) : BaseViewModel<SettingsEvent>() {

    val themeMode: StateFlow<ThemeMode> = prefsManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)



    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnUpdateThemeMode -> updateThemeMode(mode = event.mode)
        }
    }

    private fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsManager.updateThemeMode(mode)
        }
    }
}

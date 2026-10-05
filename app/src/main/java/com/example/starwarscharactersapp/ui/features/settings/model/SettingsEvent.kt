package com.example.starwarscharactersapp.ui.features.settings.model

import com.example.starwarscharactersapp.data.local.ThemeMode

sealed class SettingsEvent {
    data class  OnUpdateThemeMode(val mode: ThemeMode) : SettingsEvent()
}
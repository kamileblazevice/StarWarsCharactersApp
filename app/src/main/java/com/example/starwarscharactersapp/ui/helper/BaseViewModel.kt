package com.example.starwarscharactersapp.ui.helper

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<UiEvent> : ViewModel() {

    abstract fun onEvent(event: UiEvent)
}

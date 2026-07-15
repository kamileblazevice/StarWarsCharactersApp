package com.example.starwarscharactersapp.ui.helper

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

abstract class BaseViewModel<UiEvent> : ViewModel() {

    abstract fun onEvent(event: UiEvent)

    // Avoids flashing the error state when a failure resolves almost instantly.
    protected suspend fun delayBeforeShowingError() = delay(MIN_ERROR_DISPLAY_DELAY_MS)

    private companion object {
        const val MIN_ERROR_DISPLAY_DELAY_MS = 1000L
    }
}

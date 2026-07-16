package com.example.starwarscharactersapp.domain.model

sealed interface SyncProgress {
    data class InProgress(val completed: Int, val total: Int) : SyncProgress
    data object Success : SyncProgress
    data object Failure : SyncProgress
}

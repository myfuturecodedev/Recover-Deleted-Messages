package com.futurecode.recoverdeletedmessages.utils


/**
 * Universal state pattern wrapper to coordinate UI progress loaders cleanly.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}
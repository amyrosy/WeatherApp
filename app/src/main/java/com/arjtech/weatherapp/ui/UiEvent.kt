package com.arjtech.weatherapp.ui

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}

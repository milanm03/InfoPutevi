package com.example.rmas.presentation.marker

import android.net.Uri

sealed class MarkerUIEvent {
    data class DescriptionChanged(val description: String) : MarkerUIEvent()
    data class TitleChanged(val title: String) : MarkerUIEvent()
    data class TypeChanged(val type: String) : MarkerUIEvent()
    data class ImageChanged(val image: Uri) : MarkerUIEvent()
    object AddMarkerClicked : MarkerUIEvent()
}
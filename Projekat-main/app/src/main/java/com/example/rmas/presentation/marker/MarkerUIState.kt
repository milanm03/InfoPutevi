package com.example.rmas.presentation.marker

import android.net.Uri

data class MarkerUIState(
    var title: String = "",
    var description: String = "",
    var type: String = "Izaberite kategoriju",
    var image: Uri = Uri.EMPTY,

    var titleError: String? = null,
    var descriptionError: String? = null,
    var imageError: String? = null
)
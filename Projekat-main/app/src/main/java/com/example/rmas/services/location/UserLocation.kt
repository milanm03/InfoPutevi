package com.example.rmas.services.location

import android.location.Location
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object UserLocation {
    var location = mutableStateOf<Location?>(null)
}
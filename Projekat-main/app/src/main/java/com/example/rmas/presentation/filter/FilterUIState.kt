package com.example.rmas.presentation.filter

data class FilterUIState(
    var startDate: Long? = null,
    var endDate: Long? = null,
    var datum: String = "Izaberite opseg",
    var types: List<String> = emptyList(),
    var users: List<String> = emptyList(),
    var searchText: String = "",
    var distance: Float? = null,
)

package com.example.rmas.presentation.filter

sealed class FilterUIEvent {
    data class StartDateChanged(val startDate: Long?) : FilterUIEvent()
    data class EndDateChanged(val endDate: Long?) : FilterUIEvent()
    data class TypeChanged(val type: String) : FilterUIEvent()
    data class DistanceChanged(val distance: Float?) : FilterUIEvent()
    data class DatumChanged(val datum: String) : FilterUIEvent()
    data class UsersChanged(val user: String) : FilterUIEvent()
    data class SearchTextChanged(val searchText: String) : FilterUIEvent()
    object ResetButtonClicked : FilterUIEvent()
    object ResetUsersClicked : FilterUIEvent()
}
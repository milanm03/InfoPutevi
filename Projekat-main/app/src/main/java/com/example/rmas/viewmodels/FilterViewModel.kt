package com.example.rmas.viewmodels

import androidx.lifecycle.ViewModel
import com.example.rmas.presentation.filter.FilterUIEvent
import com.example.rmas.presentation.filter.FilterUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterViewModel : ViewModel() {

    private val _filterUIState = MutableStateFlow(FilterUIState())
    val filterUIState = _filterUIState.asStateFlow()

    fun onEvent(event: FilterUIEvent) {
        when (event) {
            is FilterUIEvent.StartDateChanged -> {
                _filterUIState.value = _filterUIState.value.copy(
                    startDate = event.startDate
                )
            }

            is FilterUIEvent.EndDateChanged -> {
                _filterUIState.value = _filterUIState.value.copy(
                    endDate = event.endDate
                )
            }

            is FilterUIEvent.TypeChanged -> {
                val currentTypes = _filterUIState.value.types.toMutableList()
                if (currentTypes.contains(event.type)) {
                    currentTypes.remove(event.type)
                } else {
                    currentTypes.add(event.type)
                }
                _filterUIState.value = _filterUIState.value.copy(
                    types = currentTypes
                )
            }

            is FilterUIEvent.DistanceChanged -> {
                _filterUIState.value = _filterUIState.value.copy(
                    distance = event.distance
                )
            }

            is FilterUIEvent.DatumChanged -> {
                _filterUIState.value = _filterUIState.value.copy(
                    datum = event.datum
                )
            }

            is FilterUIEvent.UsersChanged -> {
                val currentUsers = _filterUIState.value.users.toMutableList()
                if (currentUsers.contains(event.user)) {
                    currentUsers.remove(event.user)
                } else {
                    currentUsers.add(event.user)
                }
                _filterUIState.value = _filterUIState.value.copy(
                    users = currentUsers
                )
            }

            FilterUIEvent.ResetButtonClicked -> {
                _filterUIState.value.users = emptyList()
                _filterUIState.value.types = emptyList()
                _filterUIState.value.endDate = null
                _filterUIState.value.startDate = null
                _filterUIState.value.distance = null
                _filterUIState.value.datum = "Izaberite opseg"
                _filterUIState.value.searchText = ""
            }

            is FilterUIEvent.SearchTextChanged -> {
                _filterUIState.value = _filterUIState.value.copy(
                    searchText = event.searchText
                )
            }

            FilterUIEvent.ResetUsersClicked -> {
                _filterUIState.value.users = emptyList()
            }
        }
    }
}
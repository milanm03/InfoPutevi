package com.example.rmas.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.rmas.data.Location
import com.example.rmas.presentation.marker.MarkerUIEvent
import com.example.rmas.presentation.marker.MarkerUIState
import com.example.rmas.presentation.validation.Validator
import com.example.rmas.services.location.UserLocation
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class MarkerViewModel : ViewModel() {

    private val _markerUIState = MutableStateFlow(MarkerUIState())
    val markerUIState = _markerUIState.asStateFlow()

    private var allValidationsPassed = mutableStateOf(false)
    var addInProgress = mutableStateOf(false)
    fun onEvent(event: MarkerUIEvent, context: Context, onClick: () -> Unit) {
        when (event) {
            is MarkerUIEvent.TitleChanged -> {
                _markerUIState.value = _markerUIState.value.copy(
                    title = event.title
                )
            }

            is MarkerUIEvent.DescriptionChanged -> {
                _markerUIState.value = _markerUIState.value.copy(
                    description = event.description
                )
            }

            is MarkerUIEvent.TypeChanged -> {
                _markerUIState.value = _markerUIState.value.copy(
                    type = event.type
                )
            }

            is MarkerUIEvent.ImageChanged -> {
                _markerUIState.value = _markerUIState.value.copy(
                    image = event.image
                )
            }

            is MarkerUIEvent.AddMarkerClicked -> {
                addMarkerToDb(context, onClick)
            }
        }
    }

    private fun addMarkerToDb(context: Context, navigateToHome: () -> Unit) {
        validateData()
        if (!allValidationsPassed.value)
            return
        if (UserLocation.location.value == null) {
            Toast.makeText(
                context,
                "Potrebno je da uključite praćenje lokacije da biste mogli da dodate lokaciju na mapi.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        addInProgress.value = true
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.firestore
        val storage = FirebaseStorage.getInstance().reference.child("slike_lokacija")
            .child(System.currentTimeMillis().toString())
        var url: String
        storage.putFile(_markerUIState.value.image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.downloadUrl
                        .addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                url = it1.result.toString()
                                val ref = db.collection("locations").document()
                                val location = Location(
                                    id = ref.id,
                                    userId = userId,
                                    description = _markerUIState.value.description,
                                    title = _markerUIState.value.title,
                                    image = url,
                                    date = Timestamp(Date()),
                                    type = _markerUIState.value.type,
                                    latitude = UserLocation.location.value!!.latitude,
                                    longitude = UserLocation.location.value!!.longitude
                                )
                                ref.set(location)
                                    .addOnCompleteListener {
                                        Toast.makeText(
                                            context,
                                            "Uspešno dodata lokacija na mapu.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        addPoints(context, 5L, onClick = { navigateToHome() })
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Došlo je do greške prilikom dodavanja lokacije na mapu.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("TAG", e.localizedMessage)
                                    }
                            }
                        }
                        .addOnFailureListener { ex ->
                            Toast.makeText(
                                context,
                                "Došlo je do greške prilikom dodavanja lokacije na mapu.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("TAG", ex.localizedMessage)
                        }
                }
                addInProgress.value = false
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Došlo je do greške prilikom dodavanja lokacije na mapu.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", it.localizedMessage)
                addInProgress.value = false
            }
    }

    private fun addPoints(context: Context, points: Long, onClick: () -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.firestore
        db.collection("users").document(uid).update("points", FieldValue.increment(points))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Dobili ste dodatnih 5 poena!", Toast.LENGTH_SHORT)
                        .show()
                }
                onClick.invoke()
            }
            .addOnFailureListener {
                Log.d("TAG", it.localizedMessage)
            }
    }

    private fun validateData() {
        val titleResult = Validator.validateTitle(title = _markerUIState.value.title)
        val descriptionResult =
            Validator.validateDescription(description = _markerUIState.value.description)
        val imageResult = Validator.validateImage(image = _markerUIState.value.image)

        allValidationsPassed.value =
            (titleResult.status && descriptionResult.status && imageResult.status)
        _markerUIState.value = _markerUIState.value.copy(
            titleError = titleResult.errorMessage,
            descriptionError = descriptionResult.errorMessage,
            imageError = imageResult.errorMessage
        )
    }
}
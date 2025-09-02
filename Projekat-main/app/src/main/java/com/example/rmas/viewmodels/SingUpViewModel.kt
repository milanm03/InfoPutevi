package com.example.rmas.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.rmas.data.User
import com.example.rmas.presentation.singup.SingUpUIEvent
import com.example.rmas.presentation.singup.SingUpUIState
import com.example.rmas.presentation.validation.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SingUpViewModel : ViewModel() {
    private val _singUpUIState = MutableStateFlow(SingUpUIState())
    val singUpUIState = _singUpUIState.asStateFlow()
    private var allValidationsPassed = mutableStateOf(false)
    var singUpInProgress = mutableStateOf(false)

    fun onEvent(event: SingUpUIEvent, context: Context, navigateToLogin: () -> Unit) {
        when (event) {
            is SingUpUIEvent.ImeChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    ime = event.ime
                )
            }

            is SingUpUIEvent.PrezimeChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    prezime = event.prezime
                )
            }

            is SingUpUIEvent.EmailChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    email = event.email
                )
            }

            is SingUpUIEvent.TelefonChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    telefon = event.telefon
                )
            }

            is SingUpUIEvent.UsernameChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    username = event.username
                )
            }

            is SingUpUIEvent.PasswordChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    password = event.password
                )
            }

            is SingUpUIEvent.RegisterButtonClicked -> {
                singUp(context, navigateToLogin = { navigateToLogin() })
            }

            is SingUpUIEvent.ImageChanged -> {
                _singUpUIState.value = _singUpUIState.value.copy(
                    image = event.image
                )
            }
        }
    }

    private fun singUp(context: Context, navigateToLogin: () -> Unit) {
        validateData()
        if (!allValidationsPassed.value)
            return
        createUser(
            context,
            navigateToLogin = { navigateToLogin() })
    }

    private fun createUser(context: Context, navigateToLogin: () -> Unit) {
        singUpInProgress.value = true
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("username", _singUpUIState.value.username).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.isEmpty) {
                        FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(
                                _singUpUIState.value.email,
                                _singUpUIState.value.password
                            )
                            .addOnCompleteListener { res ->
                                if (res.isSuccessful) {
                                    FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
                                        .addOnCompleteListener {
                                            addToDatabase(
                                                context,
                                                id = FirebaseAuth.getInstance().currentUser!!.uid,
                                                ime = _singUpUIState.value.ime,
                                                prezime = _singUpUIState.value.prezime,
                                                email = _singUpUIState.value.email,
                                                username = _singUpUIState.value.username,
                                                telefon = _singUpUIState.value.telefon,
                                                image = _singUpUIState.value.image,
                                                onNavigate = { navigateToLogin() }
                                            )
                                        }
                                        .addOnFailureListener { ex ->
                                            Toast.makeText(
                                                context,
                                                "Došlo je do greške prilikom kreiranja naloga.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            FirebaseAuth.getInstance().currentUser!!.delete()
                                            Log.d("TAG", ex.localizedMessage)
                                            singUpInProgress.value = false
                                        }
                                }
                            }
                            .addOnFailureListener { ex ->
                                Toast.makeText(
                                    context,
                                    "Došlo je do greške prilikom kreiranja naloga.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("TAG", ex.localizedMessage)
                                singUpInProgress.value = false
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Uneto korisničko ime već postoji",
                            Toast.LENGTH_SHORT
                        ).show()
                        singUpInProgress.value = false
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Došlo je do greške prilikom kreiranja naloga.",
                        Toast.LENGTH_SHORT
                    ).show()
                    singUpInProgress.value = false
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Došlo je do greške prilikom kreiranja naloga.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", it.localizedMessage)
                singUpInProgress.value = false
            }

    }

    private fun addToDatabase(
        context: Context,
        id: String,
        ime: String,
        prezime: String,
        email: String,
        username: String,
        telefon: String,
        image: Uri,
        onNavigate: () -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.firestore
        val storage = FirebaseStorage.getInstance().reference.child("slike_korisnika")
            .child(uid)
        var url: String
        storage.putFile(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.downloadUrl
                        .addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                url = it1.result.toString()
                                val user = User(id, username, ime, prezime, telefon, url, email, 0L)
                                db.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnFailureListener { ex ->
                                        Log.d("TAG", ex.localizedMessage)
                                        currentUser?.delete()
                                        FirebaseStorage.getInstance().reference.child("slike_korisnika")
                                            .child(uid).delete()
                                        Toast.makeText(
                                            context,
                                            "Došlo je do greške prilikom kreiranja naloga.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        singUpInProgress.value = false
                                    }
                                    .addOnCompleteListener { res ->
                                        if (res.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Email verification link je poslat na vašu email adresu.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            FirebaseAuth.getInstance().signOut()
                                            onNavigate.invoke()
                                        }
                                        singUpInProgress.value = false
                                    }
                            } else {
                                currentUser?.delete()
                                Toast.makeText(
                                    context,
                                    "Došlo je do greške prilikom kreiranja naloga.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                singUpInProgress.value = false
                            }
                        }
                        .addOnFailureListener { e ->
                            currentUser?.delete()
                            Toast.makeText(
                                context,
                                "Došlo je do greške prilikom kreiranja naloga.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("TAG", e.localizedMessage)
                            singUpInProgress.value = false
                        }
                } else {
                    currentUser?.delete()
                    Toast.makeText(
                        context,
                        "Došlo je do greške prilikom kreiranja naloga.",
                        Toast.LENGTH_SHORT
                    ).show()
                    singUpInProgress.value = false
                }
            }
            .addOnFailureListener {
                currentUser?.delete()
                Toast.makeText(
                    context,
                    "Došlo je do greške prilikom kreiranja naloga.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", it.localizedMessage)
                singUpInProgress.value = false
            }
    }

    private fun validateData() {
        val imeResult = Validator.validateIme(ime = _singUpUIState.value.ime)
        val prezimeResult = Validator.validatePrezime(prezime = _singUpUIState.value.prezime)
        val emailResult = Validator.validateEmail(email = _singUpUIState.value.email)
        val usernameResult = Validator.validateUsername(username = _singUpUIState.value.username)
        val passwordResult = Validator.validatePassword(password = _singUpUIState.value.password)
        val telefonResult = Validator.validateTelefon(telefon = _singUpUIState.value.telefon)
        val imageResult = Validator.validateImage(image = _singUpUIState.value.image)

        allValidationsPassed.value =
            (imeResult.status && prezimeResult.status && emailResult.status && passwordResult.status
                    && telefonResult.status && usernameResult.status && imageResult.status)

        _singUpUIState.value = _singUpUIState.value.copy(
            imeError = imeResult.errorMessage,
            prezimeError = prezimeResult.errorMessage,
            emailError = emailResult.errorMessage,
            passwordError = passwordResult.errorMessage,
            usernameError = usernameResult.errorMessage,
            telefonError = telefonResult.errorMessage,
            imageError = imageResult.errorMessage
        )

    }
}
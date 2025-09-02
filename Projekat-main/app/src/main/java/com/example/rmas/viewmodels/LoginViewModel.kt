package com.example.rmas.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.rmas.presentation.login.LoginUIEvent
import com.example.rmas.presentation.login.LoginUIState
import com.example.rmas.presentation.validation.Validator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _loginUIState = MutableStateFlow(LoginUIState())
    val loginUIState = _loginUIState.asStateFlow()

    private var allValidationsPassed = mutableStateOf(false)
    var loginInProgress = mutableStateOf(false)

    fun onEvent(event: LoginUIEvent, context: Context, navigateToHome: () -> Unit) {
        when (event) {
            is LoginUIEvent.UsernameChanged -> {
                _loginUIState.value = _loginUIState.value.copy(
                    username = event.username
                )
            }

            is LoginUIEvent.PasswordChanged -> {
                _loginUIState.value = _loginUIState.value.copy(
                    password = event.password
                )
            }

            is LoginUIEvent.LoginButtonClicked -> {
                login(context, navigateToHome = { navigateToHome() })
            }
        }
    }

    private fun login(context: Context, navigateToHome: () -> Unit) {
        validateData()
        if (!allValidationsPassed.value)
            return
        loginInProgress.value = true
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("username", _loginUIState.value.username).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (!it.result.isEmpty) {
                        val email = it.result.documents[0].get("email").toString()
                        FirebaseAuth
                            .getInstance()
                            .signInWithEmailAndPassword(
                                email,
                                _loginUIState.value.password
                            )
                            .addOnFailureListener { e ->
                                Log.d("TAG", e.localizedMessage)
                                Toast.makeText(
                                    context,
                                    "Došlo je do greške prilikom prijavljivanja. Proverite da li ste uneli ispravnu šifru.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                loginInProgress.value = false
                            }
                            .addOnCompleteListener { res ->
                                if (res.isSuccessful) {
                                    if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == false) {
                                        Toast.makeText(
                                            context,
                                            "Molimo vas potvrdite email adresu.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Uspešno prijavljivanje.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navigateToHome.invoke()
                                        _loginUIState.value = LoginUIState()
                                    }
                                }
                                loginInProgress.value = false
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Ne postoji nalog sa unetim korisničkim imenom",
                            Toast.LENGTH_SHORT
                        ).show()
                        loginInProgress.value = false
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Došlo je do greške prilikom prijavljivanja.",
                        Toast.LENGTH_SHORT
                    ).show()
                    loginInProgress.value = false
                }
            }
            .addOnFailureListener {
                Log.d("TAG", it.localizedMessage)
                Toast.makeText(
                    context,
                    "Došlo je do greške prilikom prijavljivanja.",
                    Toast.LENGTH_SHORT
                ).show()
                loginInProgress.value = false
            }
    }

    fun logOut(context: Context, navigateToLogin: () -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        if (firebaseAuth.currentUser == null) {
            Toast.makeText(context, "Uspešno odjavljivanje.", Toast.LENGTH_SHORT).show()
        }
        navigateToLogin.invoke()
    }

    private fun validateData() {
        val usernameResult = Validator.validateUsername(username = _loginUIState.value.username)
        val passwordResult =
            Validator.validatePasswordLogin(password = _loginUIState.value.password)

        allValidationsPassed.value = (usernameResult.status && passwordResult.status)
        _loginUIState.value = loginUIState.value.copy(
            usernameError = usernameResult.errorMessage,
            passwordError = passwordResult.errorMessage
        )
    }
}

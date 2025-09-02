package com.example.rmas.presentation.login

data class LoginUIState(
    var username: String = "",
    var password: String = "",

    var usernameError: String? = null,
    var passwordError: String? = null
)
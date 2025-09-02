package com.example.rmas.presentation.singup

import android.net.Uri

data class SingUpUIState(
    var ime: String = "",
    var prezime: String = "",
    var email: String = "",
    var username: String = "",
    var telefon: String = "",
    var password: String = "",
    var image: Uri = Uri.EMPTY,

    var imeError: String? = null,
    var prezimeError: String? = null,
    var emailError: String? = null,
    var usernameError: String? = null,
    var telefonError: String? = null,
    var passwordError: String? = null,
    var imageError: String? = null
)
package com.example.rmas.presentation.singup

import android.net.Uri

sealed class SingUpUIEvent {
    data class ImeChanged(val ime: String) : SingUpUIEvent()
    data class PrezimeChanged(val prezime: String) : SingUpUIEvent()
    data class TelefonChanged(val telefon: String) : SingUpUIEvent()
    data class UsernameChanged(val username: String) : SingUpUIEvent()
    data class EmailChanged(val email: String) : SingUpUIEvent()
    data class PasswordChanged(val password: String) : SingUpUIEvent()
    data class ImageChanged(val image: Uri) : SingUpUIEvent()
    object RegisterButtonClicked : SingUpUIEvent()
}
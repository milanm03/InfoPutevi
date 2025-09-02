package com.example.rmas.presentation.validation

import android.net.Uri
import android.util.Patterns
import java.util.regex.Matcher
import java.util.regex.Pattern


object Validator {
    fun validateIme(ime: String): ValidationResult {
        return if (ime.isNotEmpty() && ime.length <= 30)
            ValidationResult(true)
        else
            ValidationResult(false, "Ime ne sme biti prazno.")
    }

    fun validatePrezime(prezime: String): ValidationResult {
        return if (prezime.isNotEmpty() && prezime.length <= 30)
            ValidationResult(true)
        else
            ValidationResult(false, "Prezime ne sme biti prazno.")
    }

    fun validateEmail(email: String): ValidationResult {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            ValidationResult(true)
        else
            ValidationResult(false, "Nevalidna email adresa.")
    }

    fun validatePassword(password: String): ValidationResult {
        return if (password.isNotEmpty() && isValidPassword(password))
            ValidationResult(true)
        else
            ValidationResult(
                false,
                "Šifra mora imati šest karaktera, jedan broj, jedan specijalan karakter i jedno veliko slovo."
            )
    }

    fun validateUsername(username: String): ValidationResult {
        return if (username.isNotEmpty())
            ValidationResult(true)
        else
            ValidationResult(false, "Korisničko ime ne sme biti prazno.")
    }

    fun validatePasswordLogin(password: String): ValidationResult {
        return if (password.isNotEmpty())
            ValidationResult(true)
        else
            ValidationResult(false, "Šifra ne sme biti prazna.")
    }

    fun validateTelefon(telefon: String): ValidationResult {
        return if (telefon.isNotEmpty() && isValidPhone(telefon))
            ValidationResult(true)
        else
            ValidationResult(false, "Nevalidan broj telefona.")
    }

    fun validateImage(image: Uri): ValidationResult {
        return if (image != Uri.EMPTY)
            ValidationResult(true)
        else
            ValidationResult(false, "Potrebno je izabrati jednu sliku.")
    }

    fun validateTitle(title: String): ValidationResult {
        return if (title.isNotEmpty())
            ValidationResult(true)
        else
            ValidationResult(false, "Naslov ne sme biti prazan.")
    }

    fun validateDescription(description: String): ValidationResult {
        return if (description.isNotEmpty())
            ValidationResult(true)
        else
            ValidationResult(false, "Opis ne sme biti prazan.")
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern: Pattern
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$"
        pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    private fun isValidPhone(telefon: String): Boolean {
        val pattern: Pattern
        val phonePattern = "^(\\+3816|06)(([0-6]|[8-9])\\d{6,7}|(77|78)\\d{5,6})$"
        pattern = Pattern.compile(phonePattern)
        val matcher: Matcher = pattern.matcher(telefon)
        return matcher.matches()
    }
}

data class ValidationResult(
    val status: Boolean = false,
    val errorMessage: String? = null
)
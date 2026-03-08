package com.example.trackpoints.presentation.auth

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false,
    val hasValidationErrors: Boolean = false,
    val hasConfirmPasswordError: Boolean = false
)

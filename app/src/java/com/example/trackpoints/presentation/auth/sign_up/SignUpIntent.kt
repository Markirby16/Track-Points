package com.example.trackpoints.presentation.auth.sign_up

sealed interface SignUpIntent {
    data class FullNameChanged(val value: String) : SignUpIntent
    data class EmailChanged(val value: String) : SignUpIntent
    data class EmailCodeChanged(val value: String) : SignUpIntent
    data class PasswordChanged(val value: String) : SignUpIntent
    data class ConfirmPasswordChanged(val value: String) : SignUpIntent
    data class RoleChanged(val value: String) : SignUpIntent
    data class IsLoadingChanged(val value: Boolean) : SignUpIntent

    data class FullNameErrorChanged(val value: String) : SignUpIntent
    data class EmailErrorChanged(val value: String) : SignUpIntent
    data class EmailCodeErrorChanged(val value: String) : SignUpIntent
    data class PasswordErrorChanged(val value: String) : SignUpIntent
    data class ConfirmPasswordErrorChanged(val value: String) : SignUpIntent
    data class SignUpErrorChanged(val value: String) : SignUpIntent

    data object NextOfVerificationClicked : SignUpIntent
    data object BackClicked : SignUpIntent
    data object LoginClicked : SignUpIntent
    data object ResendCode : SignUpIntent
    data object VerifyOtp : SignUpIntent
    data object SignUpClicked : SignUpIntent
}

sealed interface SignUpEffect {
    data object NavigateToHome: SignUpEffect
    data object NavigateBack : SignUpEffect
    data object NavigateToLogin : SignUpEffect
    data object NavigateToNext : SignUpEffect
}

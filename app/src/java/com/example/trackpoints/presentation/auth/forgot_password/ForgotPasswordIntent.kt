package com.example.trackpoints.presentation.auth.forgot_password

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val value: String) : ForgotPasswordIntent
    data class EmailErrorChanged(val value: String) : ForgotPasswordIntent
    data class EmailCodeChanged(val value: String) : ForgotPasswordIntent
    data class EmailCodeErrorChanged(val value: String) : ForgotPasswordIntent
    data class NewPasswordChanged(val value: String) : ForgotPasswordIntent
    data class NewPasswordErrorChanged(val value: String) : ForgotPasswordIntent
    data class ConfirmNewPasswordChanged(val value: String) : ForgotPasswordIntent
    data class ConfirmNewPasswordErrorChanged(val value: String) : ForgotPasswordIntent
    data class IsLoadingChanged(val value: Boolean) : ForgotPasswordIntent

    data object BackClicked : ForgotPasswordIntent
    data object LoginClicked : ForgotPasswordIntent
    data object NextOfForgotPasswordClicked : ForgotPasswordIntent
    data object SubmitResetPasswordClicked : ForgotPasswordIntent
    data object ResendCode : ForgotPasswordIntent
    data object VerifyOtp : ForgotPasswordIntent
}

sealed interface ForgotPasswordEffect {
    data object NavigateBack : ForgotPasswordEffect
    data object NavigateToLogin : ForgotPasswordEffect
    data object NavigateToNext : ForgotPasswordEffect
}
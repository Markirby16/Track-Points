package com.example.trackpoints.presentation.auth.forgot_password

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String = "",
    val emailCode: String = "",
    val emailCodeError: String = "",
    val newPassword: String = "",
    val newPasswordError: String = "",
    val confirmNewPassword: String = "",
    val confirmNewPasswordError: String = "",
    val isLoading: Boolean = false,
    val actionError: String = "",
)
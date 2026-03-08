package com.example.trackpoints.presentation.auth.sign_up


data class SignUpState(
    val fullName: String = "",
    val email: String = "",
    val emailCode: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: String = "CLIENT",
    val isLoading: Boolean = false,

    val fullNameError: String = "",
    val emailError: String = "",
    val emailCodeError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val signUpError: String = "",
)

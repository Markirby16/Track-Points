package com.example.trackpoints.presentation.auth.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,

    val emailOrPasswordError: String = "",
    val loginStatus: String = ""
)
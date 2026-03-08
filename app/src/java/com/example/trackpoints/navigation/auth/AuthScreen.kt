package com.example.trackpoints.navigation.auth

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class AuthScreen : NavKey {
    @Serializable
    data object NoInternet : AuthScreen()

    @Serializable
    data object VerifyFace: AuthScreen()

    @Serializable
    data object SignUp : AuthScreen()

    @Serializable
    data object VerifyEmail : AuthScreen()

    @Serializable
    data object Login : AuthScreen()

    @Serializable
    data object ForgotPassword : AuthScreen()

    @Serializable
    data object VerifyReset : AuthScreen()

    @Serializable
    data object ResetPassword : AuthScreen()
}

package com.example.trackpoints.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class RootScreen : NavKey {
    @Serializable
    data object Splash : RootScreen()

    @Serializable
    data object Auth : RootScreen()

    @Serializable
    data object Main : RootScreen()
}

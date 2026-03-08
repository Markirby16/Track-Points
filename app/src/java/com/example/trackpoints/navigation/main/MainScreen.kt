package com.example.trackpoints.navigation.main

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class MainScreen : NavKey {
    @Serializable
    data object Main: MainScreen()

    @Serializable
    data object Chat: MainScreen()

    @Serializable
    data object Request: MainScreen()

    @Serializable
    data object Profile: MainScreen()
}

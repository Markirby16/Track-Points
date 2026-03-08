@file:JvmName("AdminBottomBarScreenKt")

package com.example.trackpoints.navigation.main

import androidx.compose.runtime.saveable.Saver
import androidx.navigation3.runtime.NavKey
import com.example.trackpoints.R
import kotlinx.serialization.Serializable

val adminBottomBarItems = listOf<AdminBottomBarScreen>(
    AdminBottomBarScreen.Home,
    AdminBottomBarScreen.Profile,
    AdminBottomBarScreen.Requests,
)

@Serializable
sealed class AdminBottomBarScreen(val icon: Int, val title: String) : NavKey {
    @Serializable
    data object Home : AdminBottomBarScreen(icon = R.drawable.home_icon, title = "Home")

    @Serializable
    data object Profile :
        AdminBottomBarScreen(icon = R.drawable.profile_icon, title = "Profile")

    @Serializable
    data object Requests :
        AdminBottomBarScreen(icon = R.drawable.requests_icon, title = "Requests")
}

val AdminBottomBarScreenSaver = Saver<AdminBottomBarScreen, String>(
    save = { it::class.simpleName ?: "Unknown" },
    restore = {
        when (it) {
            AdminBottomBarScreen.Home::class.simpleName -> AdminBottomBarScreen.Home
            AdminBottomBarScreen.Profile::class.simpleName -> AdminBottomBarScreen.Profile
            AdminBottomBarScreen.Requests::class.simpleName -> AdminBottomBarScreen.Requests
            else -> AdminBottomBarScreen.Home
        }
    },
)

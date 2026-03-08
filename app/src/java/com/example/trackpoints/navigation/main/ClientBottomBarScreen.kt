package com.example.trackpoints.navigation.main

import androidx.compose.runtime.saveable.Saver
import androidx.navigation3.runtime.NavKey
import com.example.trackpoints.R
import kotlinx.serialization.Serializable

val clientBottomBarItems = listOf<ClientBottomBarScreen>(
    ClientBottomBarScreen.Home,
    ClientBottomBarScreen.Projects,
    ClientBottomBarScreen.Freelancers,
    ClientBottomBarScreen.Points,
    ClientBottomBarScreen.Messages,
)

@Serializable
sealed class ClientBottomBarScreen(val icon: Int, val title: String) : NavKey {
    @Serializable
    data object Home : ClientBottomBarScreen(icon = R.drawable.home_icon, title = "Home")

    @Serializable
    data object Projects :
        ClientBottomBarScreen(icon = R.drawable.projects_icon, title = "Projects")

    @Serializable
    data object Freelancers :
        ClientBottomBarScreen(icon = R.drawable.freelancers_icon, title = "Freelancers")

    @Serializable
    data object Points :
        ClientBottomBarScreen(icon = R.drawable.points_icon, title = "Points")

    @Serializable
    data object Messages :
        ClientBottomBarScreen(icon = R.drawable.messages_icon, title = "Messages")
}

val ClientBottomBarScreenSaver = Saver<ClientBottomBarScreen, String>(
    save = { it::class.simpleName ?: "Unknown" },
    restore = {
        when (it) {
            ClientBottomBarScreen.Home::class.simpleName -> ClientBottomBarScreen.Home
            ClientBottomBarScreen.Projects::class.simpleName -> ClientBottomBarScreen.Projects
            ClientBottomBarScreen.Freelancers::class.simpleName -> ClientBottomBarScreen.Freelancers
            ClientBottomBarScreen.Points::class.simpleName -> ClientBottomBarScreen.Points
            ClientBottomBarScreen.Messages::class.simpleName -> ClientBottomBarScreen.Messages
            else -> ClientBottomBarScreen.Home
        }
    },
)

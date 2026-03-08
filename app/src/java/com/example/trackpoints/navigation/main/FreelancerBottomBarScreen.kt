package com.example.trackpoints.navigation.main

import androidx.compose.runtime.saveable.Saver
import androidx.navigation3.runtime.NavKey
import com.example.trackpoints.R
import kotlinx.serialization.Serializable

val freelancerBottomBarItems = listOf<FreelancerBottomBarScreen>(
    FreelancerBottomBarScreen.Home,
    FreelancerBottomBarScreen.Projects,
    FreelancerBottomBarScreen.Profile,
    FreelancerBottomBarScreen.Points,
    FreelancerBottomBarScreen.Messages,
)

@Serializable
sealed class FreelancerBottomBarScreen(val icon: Int, val title: String) : NavKey {
    @Serializable
    data object Home : FreelancerBottomBarScreen(icon = R.drawable.home_icon, title = "Home")

    @Serializable
    data object Projects :
        FreelancerBottomBarScreen(icon = R.drawable.projects_icon, title = "Projects")

    @Serializable
    data object Profile :
        FreelancerBottomBarScreen(icon = R.drawable.profile_icon, title = "Profile")

    @Serializable
    data object Points :
        FreelancerBottomBarScreen(icon = R.drawable.points_icon, title = "Points")

    @Serializable
    data object Messages :
        FreelancerBottomBarScreen(icon = R.drawable.messages_icon, title = "Messages")
}

val FreelancerBottomBarScreenSaver = Saver<FreelancerBottomBarScreen, String>(
    save = { it::class.simpleName ?: "Unknown" },
    restore = {
        when (it) {
            FreelancerBottomBarScreen.Home::class.simpleName -> FreelancerBottomBarScreen.Home
            FreelancerBottomBarScreen.Projects::class.simpleName -> FreelancerBottomBarScreen.Projects
            FreelancerBottomBarScreen.Profile::class.simpleName -> FreelancerBottomBarScreen.Profile
            FreelancerBottomBarScreen.Points::class.simpleName -> FreelancerBottomBarScreen.Points
            FreelancerBottomBarScreen.Messages::class.simpleName -> FreelancerBottomBarScreen.Messages
            else -> FreelancerBottomBarScreen.Home
        }
    },
)

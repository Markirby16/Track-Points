package com.example.trackpoints.navigation.main

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.trackpoints.presentation.client.FreelancerRequestScreen
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.shared.ProfileScreen
import com.example.trackpoints.presentation.main.MainScreens
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.presentation.shared.ChatScreen

@Composable
fun MainNavigation(viewModel: MainViewModel, onLogout: () -> Unit) {
    val backStack = rememberNavBackStack(MainScreen.Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<MainScreen.Main> {
                MainScreens(
                    viewModel = viewModel,
                    onLogout = onLogout,
                    onNavigateRequest = {
                        backStack.add(MainScreen.Request)
                    },
                    onNavigateChat = {
                        backStack.add(MainScreen.Chat)
                    },
                    onNavigateProfile = {
                        backStack.add(MainScreen.Profile)
                    }
                )
            }
            entry<MainScreen.Chat> {
                ChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                        viewModel.handleIntent(MainIntent.StopChat)
                    },
                )
            }
            entry<MainScreen.Request> {
                FreelancerRequestScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                )
            }
            entry<MainScreen.Profile> {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onLogout = onLogout
                )
            }
        },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(
                targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                targetOffsetX = { it })
        },
    )
}
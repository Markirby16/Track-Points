package com.example.trackpoints.navigation

import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.trackpoints.navigation.auth.AuthNavigation
import com.example.trackpoints.navigation.main.MainNavigation
import com.example.trackpoints.presentation.SplashScreen
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.utils.NetworkConnectivityService
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RootNavigation() {
    val mainViewModel: MainViewModel = koinViewModel()
    val startingDestination = RootScreen.Splash
    val backStack = rememberNavBackStack(startingDestination)

    val networkService: NetworkConnectivityService = koinInject()
    val isOnline by networkService.observeNetworkStatus()
        .collectAsStateWithLifecycle(initialValue = true)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<RootScreen.Splash>
            {
                SplashScreen(
                    mainViewModel = mainViewModel,
                    onLoaded = { isLoggedIn ->
                        backStack.removeLastOrNull()
                        if (isLoggedIn && isOnline) {
                            backStack.add(RootScreen.Main)
                        } else {
                            backStack.add(RootScreen.Auth)
                        }
                    },
                )
            }
            entry<RootScreen.Auth> {
                AuthNavigation(onLogin = {
                    backStack.removeLastOrNull()
                    backStack.add(RootScreen.Splash)
                })
            }
            entry<RootScreen.Main> {
                MainNavigation(
                    viewModel = mainViewModel,
                    onLogout = {
                        backStack.removeLastOrNull()
                        backStack.add(RootScreen.Auth)
                    }
                )
            }
        },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
    )
}

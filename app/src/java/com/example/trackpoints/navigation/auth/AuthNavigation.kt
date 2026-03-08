package com.example.trackpoints.navigation.auth

import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.trackpoints.presentation.auth.forgot_password.ForgotPasswordViewModel
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.data.repository.AuthRepository
import com.example.trackpoints.presentation.auth.forgot_password.ForgotPasswordScreen
import com.example.trackpoints.presentation.auth.forgot_password.ResetPasswordScreen
import com.example.trackpoints.presentation.auth.forgot_password.VerifyResetScreen
import com.example.trackpoints.presentation.auth.sign_up.SignUpViewModel
import com.example.trackpoints.presentation.auth.login.LoginScreen
import com.example.trackpoints.presentation.auth.login.LoginViewModel
import com.example.trackpoints.presentation.auth.sign_up.SignUpScreen
import com.example.trackpoints.presentation.auth.sign_up.VerifyEmailScreen
import com.example.trackpoints.presentation.auth.sign_up.VerifyFaceScreen
import com.example.trackpoints.utils.NetworkConnectivityService
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AuthNavigation(onLogin: () -> Unit) {
    val signUpViewModel: SignUpViewModel = koinViewModel()
    val forgotPasswordViewModel: ForgotPasswordViewModel = koinViewModel()

    val auth: Auth = koinInject()
    val authRepository: AuthRepository = koinInject()
    val isLoggedIn = authRepository.isLoggedIn()

//    val startingDestination = AuthScreen.VerifyFace
    val startingDestination = AuthScreen.Login
    val backStack = rememberNavBackStack(startingDestination)

    val networkService: NetworkConnectivityService = koinInject()
    val isOnline by networkService.observeNetworkStatus()
        .collectAsStateWithLifecycle(initialValue = true)

    LaunchedEffect(isOnline) {
        delay(500L)
        if (!isOnline) {
            backStack.add(AuthScreen.NoInternet)
        }

        if (isOnline && isLoggedIn) {
            Log.d("SUPABASE", "User is authenticated")
            Log.d("SUPABASE", "User logged in, redirecting")
            onLogin()
        }
    }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AuthScreen.NoInternet> {
                Alert(
                    onDismissRequest = {},
                    title = "No internet connection",
                    message = "It seems like you are not connected to any networks. Please try again",
                    confirmButtonText = "Ok",
                    onConfirmClicked = { },
                )
            }
            entry<AuthScreen.VerifyFace> {
                VerifyFaceScreen(
                    signUpViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateNext = { backStack.add(AuthScreen.SignUp) },
                )
            }
            entry<AuthScreen.SignUp> {
                SignUpScreen(
                    signUpViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateNext = { backStack.add(AuthScreen.VerifyEmail) },
                    onNavigateLogin = { backStack.add(AuthScreen.Login) },
                )
            }
            entry<AuthScreen.VerifyEmail> {
                VerifyEmailScreen(
                    signUpViewModel,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateHome = { onLogin() },
                    onNavigateLogin = { backStack.add(AuthScreen.Login) },
                    onNavigateNext = {
                        backStack.clear()
                        backStack.add(AuthScreen.SignUp)
                        backStack.add(AuthScreen.Login)
                    },
                )
            }
            entry<AuthScreen.Login> {
                val loginViewModel: LoginViewModel = koinViewModel()

                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateSignUp = {
                        backStack.add(AuthScreen.VerifyFace)
                    },
                    onNavigateForgotPassword = {
                        backStack.add(AuthScreen.ForgotPassword)
                    },
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onLogin = {
                        onLogin()
                    }
                )
            }
            entry<AuthScreen.ForgotPassword> {
                ForgotPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateNext = {
                        backStack.add(AuthScreen.VerifyReset)
                    },
                )
            }
            entry<AuthScreen.VerifyReset> {
                VerifyResetScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateNext = {
                        backStack.add(AuthScreen.ResetPassword)
                    },
                )
            }
            entry<AuthScreen.ResetPassword> {
                ResetPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateNext = {
                        backStack.clear()
                        backStack.add(AuthScreen.SignUp)
                        backStack.add(AuthScreen.Login)
                    },
                )
            }
        },
        transitionSpec = {
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

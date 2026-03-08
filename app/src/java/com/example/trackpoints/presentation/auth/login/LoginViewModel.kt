package com.example.trackpoints.presentation.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.data.repository.AuthRepository
import com.example.trackpoints.data.repository.UserRepository
import com.example.trackpoints.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> _state.update { it.copy(email = intent.value) }
            is LoginIntent.PasswordChanged -> _state.update { it.copy(password = intent.value) }
            is LoginIntent.EmailOrPasswordErrorChanged -> _state.update {
                it.copy(emailOrPasswordError = intent.value)
            }

            is LoginIntent.IsLoadingChanged -> _state.update { it.copy(isLoading = intent.value) }
            is LoginIntent.LoginStatusChanged -> _state.update { it.copy(loginStatus = intent.value) }

            is LoginIntent.SignUpClicked -> sendEffect(LoginEffect.NavigateToSignUp)
            is LoginIntent.BackClicked -> sendEffect(LoginEffect.NavigateBack)
            is LoginIntent.ForgotPasswordClicked -> sendEffect(LoginEffect.NavigateToForgotPassword)
            is LoginIntent.LoginClicked -> login()
        }
    }

    private fun login() {
        val currentState = _state.value
        var emailOrPasswordError = Utils.validateInput("emailLogin", currentState.email)

        _state.update {
            it.copy(emailOrPasswordError = emailOrPasswordError)
        }

        if (emailOrPasswordError.isNotBlank()) {
            handleIntent(LoginIntent.IsLoadingChanged(false))
            return
        }

        viewModelScope.launch {
            val doesExist = authRepository.checkIfEmailExists(currentState.email)

            emailOrPasswordError = when (doesExist) {
                is ApiResult.Success -> {
                    if (!doesExist.data) "User not found" else ""
                }

                is ApiResult.Error -> {
                    doesExist.message
                }
            }

            _state.update {
                it.copy(emailOrPasswordError = emailOrPasswordError)
            }

            if (emailOrPasswordError.isNotBlank()) {
                handleIntent(LoginIntent.IsLoadingChanged(false))
            } else {
                val signInResult = authRepository.signIn(currentState.email, currentState.password)

                when (signInResult) {
                    is ApiResult.Success -> {}
                    is ApiResult.Error -> {
                        emailOrPasswordError = signInResult.message
                    }
                }

                _state.update {
                    it.copy(emailOrPasswordError = emailOrPasswordError)
                }

                if (_state.value.emailOrPasswordError.isBlank()) {
                    val result = userRepository.getCurrentUserProfile()
                    var user: User? = null

                    when (result) {
                        is ApiResult.Success -> user = result.data
                        is ApiResult.Error -> Log.d("SUPABASE", result.message)
                    }

                    if (user != null && user.isApproved) {
                        delay(500)
                        sendEffect(LoginEffect.NavigateToNext)
                    } else if (user != null && user.isRejected) {
                        handleIntent(LoginIntent.LoginStatusChanged("Admin rejected your account request"))
                    } else {
                        handleIntent(LoginIntent.LoginStatusChanged("Wait for admin's approval of your account"))
                    }
                }
                handleIntent(LoginIntent.IsLoadingChanged(false))
            }
        }
    }

    private fun sendEffect(effect: LoginEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
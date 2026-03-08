package com.example.trackpoints.presentation.auth.forgot_password

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.data.repository.AuthRepository
import com.example.trackpoints.utils.Utils
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ForgotPasswordEffect>()
    val effect: SharedFlow<ForgotPasswordEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> _state.update { it.copy(email = intent.value) }
            is ForgotPasswordIntent.EmailErrorChanged -> _state.update { it.copy(emailError = intent.value) }
            is ForgotPasswordIntent.EmailCodeChanged -> _state.update { it.copy(emailCode = intent.value) }
            is ForgotPasswordIntent.EmailCodeErrorChanged -> _state.update { it.copy(emailCodeError = intent.value) }
            is ForgotPasswordIntent.NewPasswordChanged -> _state.update { it.copy(newPassword = intent.value) }
            is ForgotPasswordIntent.NewPasswordErrorChanged -> _state.update {
                it.copy(
                    newPasswordError = intent.value
                )
            }

            is ForgotPasswordIntent.ConfirmNewPasswordChanged -> _state.update {
                it.copy(
                    confirmNewPassword = intent.value
                )
            }

            is ForgotPasswordIntent.ConfirmNewPasswordErrorChanged -> _state.update {
                it.copy(
                    confirmNewPasswordError = intent.value
                )
            }

            is ForgotPasswordIntent.IsLoadingChanged -> _state.update { it.copy(isLoading = intent.value) }

            is ForgotPasswordIntent.BackClicked -> sendEffect(ForgotPasswordEffect.NavigateBack)
            is ForgotPasswordIntent.LoginClicked -> sendEffect(ForgotPasswordEffect.NavigateToLogin)
            is ForgotPasswordIntent.NextOfForgotPasswordClicked -> validateForgotPassword()
            is ForgotPasswordIntent.SubmitResetPasswordClicked -> validateResetPassword()
            is ForgotPasswordIntent.ResendCode -> resendCode()
            is ForgotPasswordIntent.VerifyOtp -> verifyOtp()
        }
    }

    private fun resendCode() {
        val currentState = _state.value
        viewModelScope.launch {
            authRepository.sendPasswordReset(currentState.email)
        }
    }

    private fun verifyOtp() {
        val currentState = _state.value

        viewModelScope.launch {
            var emailCodeError = ""
            val result =
                authRepository.verifyOtp(
                    OtpType.Email.RECOVERY,
                    currentState.email,
                    currentState.emailCode
                )

            when (result) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> {
                    emailCodeError = result.message
                    _state.update { it.copy(emailCodeError = emailCodeError) }
                }
            }

            if (emailCodeError.isBlank()) {
                delay(500)
                sendEffect(ForgotPasswordEffect.NavigateToNext)
            }
            handleIntent(ForgotPasswordIntent.IsLoadingChanged(false))
        }
    }

    private fun validateResetPassword() {
        val currentState = _state.value
        val newPasswordError = Utils.validateInput("password", currentState.newPassword)
        val confirmNewPasswordError =
            if (currentState.newPassword != currentState.confirmNewPassword ||
                (currentState.newPassword.isBlank() && currentState.confirmNewPassword.isBlank())
            ) "Passwords do not match" else ""


        _state.update {
            it.copy(
                newPasswordError = newPasswordError,
                confirmNewPasswordError = confirmNewPasswordError
            )
        }

        if (newPasswordError.isNotBlank() || confirmNewPasswordError.isNotBlank()) {
            handleIntent(ForgotPasswordIntent.IsLoadingChanged(false))
            return
        }

        viewModelScope.launch {
            val result = authRepository.confirmPasswordReset(currentState.newPassword)
            var confirmNewPasswordError = ""

            when (result) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> {
                    confirmNewPasswordError = result.message
                    _state.update { it.copy(confirmNewPasswordError = confirmNewPasswordError) }
                }
            }
            Log.d("SUPABASE", confirmNewPasswordError)

            if (confirmNewPasswordError.isBlank()) {
                delay(500)
                sendEffect(ForgotPasswordEffect.NavigateToNext)
            }
            handleIntent(ForgotPasswordIntent.IsLoadingChanged(false))
        }
    }

    private fun validateForgotPassword() {
        val currentState = _state.value
        var emailError = Utils.validateInput("emailForgotPassword", currentState.email)

        _state.update { it.copy(emailError = emailError) }

        if (emailError.isNotBlank()) {
            handleIntent(ForgotPasswordIntent.IsLoadingChanged(false))
            return
        }

        viewModelScope.launch {
            val doesExist = authRepository.checkIfEmailExists(currentState.email)

            emailError = when (doesExist) {
                is ApiResult.Success -> {
                    if (!doesExist.data) "User not found" else ""
                }

                is ApiResult.Error -> {
                    doesExist.message
                }
            }

            _state.update {
                it.copy(emailError = emailError)
            }

            if (emailError.isNotBlank()) {
                handleIntent(ForgotPasswordIntent.IsLoadingChanged(false))
            } else {
                val sendEmailReset = authRepository.sendPasswordReset(currentState.email)

                when (sendEmailReset) {
                    is ApiResult.Success -> {}

                    is ApiResult.Error -> {
                        emailError = sendEmailReset.message
                    }
                }

                _state.update {
                    it.copy(emailError = emailError)
                }

                if (_state.value.emailError.isBlank()) {
                    delay(500)
                    sendEffect(ForgotPasswordEffect.NavigateToNext)
                }
                handleIntent(ForgotPasswordIntent.IsLoadingChanged(false))
            }
        }
    }

    private fun sendEffect(effect: ForgotPasswordEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
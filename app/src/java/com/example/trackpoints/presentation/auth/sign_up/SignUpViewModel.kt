package com.example.trackpoints.presentation.auth.sign_up

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackpoints.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.data.repository.UserRepository
import com.example.trackpoints.navigation.auth.AuthScreen
import com.example.trackpoints.utils.Utils
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.sign

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SignUpEffect>()
    val effect: SharedFlow<SignUpEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.FullNameChanged -> _state.update { it.copy(fullName = intent.value) }
            is SignUpIntent.EmailChanged -> _state.update { it.copy(email = intent.value) }
            is SignUpIntent.EmailCodeChanged -> _state.update { it.copy(emailCode = intent.value) }
            is SignUpIntent.PasswordChanged -> _state.update { it.copy(password = intent.value) }
            is SignUpIntent.ConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = intent.value) }
            is SignUpIntent.RoleChanged -> _state.update { it.copy(role = intent.value) }
            is SignUpIntent.IsLoadingChanged -> _state.update { it.copy(isLoading = intent.value) }

            is SignUpIntent.FullNameErrorChanged -> _state.update { it.copy(fullNameError = intent.value) }
            is SignUpIntent.EmailErrorChanged -> _state.update { it.copy(emailError = intent.value) }
            is SignUpIntent.EmailCodeErrorChanged -> _state.update { it.copy(emailCodeError = intent.value) }
            is SignUpIntent.PasswordErrorChanged -> _state.update {
                it.copy(passwordError = intent.value)
            }

            is SignUpIntent.ConfirmPasswordErrorChanged -> _state.update {
                it.copy(
                    confirmPasswordError = intent.value
                )
            }

            is SignUpIntent.SignUpErrorChanged -> _state.update { it.copy(signUpError = intent.value) }

            is SignUpIntent.NextOfVerificationClicked -> sendEffect(SignUpEffect.NavigateToNext)
            is SignUpIntent.BackClicked -> sendEffect(SignUpEffect.NavigateBack)
            is SignUpIntent.LoginClicked -> sendEffect(SignUpEffect.NavigateToLogin)

            is SignUpIntent.ResendCode -> resendCode()
            is SignUpIntent.VerifyOtp -> verifyOtp()
            is SignUpIntent.SignUpClicked -> {
                validateSignUp()
            }
        }
    }

    private fun resendCode() {
        val currentState = _state.value
        viewModelScope.launch {
            authRepository.resendOtp(OtpType.Email.SIGNUP, currentState.email)
        }
    }

    private fun verifyOtp() {
        val currentState = _state.value
        var signUpError = ""

        viewModelScope.launch {
            var emailCodeError = ""
            val result = authRepository.verifyOtp(
                OtpType.Email.SIGNUP,
                currentState.email,
                currentState.emailCode
            )

            when (result) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> emailCodeError = result.message
            }

            _state.update { it.copy(emailCodeError = emailCodeError) }

            Log.d("VERIFY_OTP", emailCodeError)

            if (emailCodeError.isBlank()) {
                val updates = buildJsonObject {
                    put("fullname", currentState.fullName)
                    put("email", currentState.email)
                    put("role", currentState.role.uppercase())
                }

                val result = userRepository.updateUserProfile(
                    authRepository.getCurrentUserId()!!,
                    updates = updates
                )

                when (result) {
                    is ApiResult.Success -> Log.d("VERIFY_OTP", result.data.toString())
                    is ApiResult.Error -> {
                        Log.d("VERIFY_OTP", result.message)
                        signUpError = result.message
                    }
                }

                _state.update { it.copy(signUpError = signUpError) }
                if (signUpError.isBlank()) {
                    delay(500)
                    handleIntent(SignUpIntent.IsLoadingChanged(false))
                    sendEffect(SignUpEffect.NavigateToLogin)
                } else {
                    handleIntent(SignUpIntent.IsLoadingChanged(false))
                }
            } else {
                handleIntent(SignUpIntent.IsLoadingChanged(false))
            }
        }
    }

    private fun signup() {
        val currentState = _state.value

        viewModelScope.launch {
            var signUpError = ""
            val result = authRepository.signUp(currentState.email, currentState.password)

            when (result) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> signUpError = result.message
            }

            _state.update { it.copy(signUpError = signUpError) }

            Log.d("SIGNUP", signUpError)

            if (signUpError.isBlank()) {
                delay(500)
                handleIntent(SignUpIntent.IsLoadingChanged(false))
                sendEffect(SignUpEffect.NavigateToNext)
            } else {
                handleIntent(SignUpIntent.IsLoadingChanged(false))
            }
        }
    }

    private fun validateSignUp() {
        val currentState = _state.value
        Log.d("SIGNUP", currentState.toString())

        val fullNameError = Utils.validateInput("fullname", currentState.fullName)
        var emailError = Utils.validateInput("email", currentState.email)
        val passwordError = Utils.validateInput("password", currentState.password)
        val confirmPasswordError =
            if (currentState.password != currentState.confirmPassword ||
                (currentState.password.isBlank() && currentState.confirmPassword.isBlank())
            ) "Passwords do not match" else ""

        _state.update {
            it.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }

        Log.d("SIGNUP", passwordError)
        if (fullNameError.isNotBlank() || emailError.isNotBlank()
            || passwordError.isNotBlank() || confirmPasswordError.isNotBlank()
        ) {
            handleIntent(SignUpIntent.IsLoadingChanged(false))
            return
        }

        viewModelScope.launch {
            val emailResult = authRepository.checkIfEmailExists(currentState.email)
            var signUpError = ""

            when (emailResult) {
                is ApiResult.Success -> {
                    emailError = if (emailResult.data) "Email already exists" else ""
                }

                is ApiResult.Error -> {
                    signUpError = emailResult.message
                }
            }

            _state.update {
                it.copy(
                    emailError = emailError,
                    signUpError = signUpError
                )
            }

            if (emailError.isBlank() && signUpError.isBlank()) {
                signup()
            } else {
                handleIntent(SignUpIntent.IsLoadingChanged(false))
            }
        }
    }

    private fun sendEffect(effect: SignUpEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}

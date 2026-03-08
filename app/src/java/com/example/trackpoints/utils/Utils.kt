package com.example.trackpoints.utils

import android.util.Log
import com.example.trackpoints.data.model.CommissionRequest
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.presentation.auth.PasswordValidationState
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object Utils {

    fun validateInput(type: String, value: String): String {
        return when (type) {
            "fullname" -> {
                if (value.length < 6) "Full name is too short (minimum is 5 characters)"
                else ""
            }

            "email" -> {
                if (value.isBlank()) "This field is required"
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value)
                        .matches()
                ) "Email is invalid"
                else ""
            }

            "username" -> {
                val usernameRegex = Regex("^[A-Za-z0-9_]{3,20}$")
                if (value.isBlank()) "This field is required"
                else if (!usernameRegex.matches(value)) "Username must have 3-20 characters, using only letters, numbers, and underscores."
                else ""
            }

            "password" -> {
                if (value.length < 6) "Password is too short (minimum is 6 characters)"
                else ""
            }

            "emailForgotPassword" -> {
                if (value.isBlank()) "This field is required"
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value)
                        .matches()
                ) "Email is invalid"
                else ""
            }

            "emailLogin" -> {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value)
                        .matches()
                ) "Email is invalid"
                else ""
            }

            else -> ""
        }
    }

    fun validatePassword(value: String, confirmPassword: String = ""): PasswordValidationState {
        val passwordValidationState = PasswordValidationState()

        val hasMinLength = value.length >= 8
        val hasUppercase = value.any { it.isUpperCase() }
        val hasLowercase = value.any { it.isLowerCase() }
        val hasDigit = value.any { it.isDigit() }
        val hasSpecialChar = value.any { !it.isLetterOrDigit() }
        val hasValidationErrors =
            value.isBlank() || !(hasUppercase && hasLowercase && hasDigit && hasSpecialChar && hasMinLength)
        val hasConfirmPasswordError = value.isBlank() || value != confirmPassword

        return passwordValidationState.copy(
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecialChar,
            hasValidationErrors = hasValidationErrors,
            hasConfirmPasswordError = hasConfirmPasswordError
        )
    }

    fun validateCommission(commissionRequest: CommissionRequest): String? {
        val name = commissionRequest.name
        val description =  commissionRequest.description
        val points = commissionRequest.points.toString()
        val deadline = commissionRequest.dueDate

        if (name.trim().length < 5) return "Name must be at least 5 characters."
        if (name.trim().length > 50) return "Name must not exceed 50 characters."

        if (description.trim().length < 20) return "Description is too short (min 20)."
        if (description.trim().length > 1000) return "Description is too long."

        val pointsInt = points.toIntOrNull()
        if (pointsInt == null) return "Points must be a valid number."
        if (pointsInt < 1000 || pointsInt > 50000000) return "Points must be between 1K and 50M."

        val tomorrowStart = LocalDate.now(ZoneId.of("Asia/Manila"))
            .plusDays(1)
            .atStartOfDay(ZoneId.of("Asia/Manila"))
            .toInstant()

        if (deadline.isBefore(tomorrowStart)) {
            return "Deadline must be tomorrow or later."
        }

        return null
    }

    suspend fun <T> dbResponseHandler(apiCall: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(apiCall())
        } catch (e: PostgrestRestException) {
            Log.d("SUPABASE_ERROR", "${e.message}")
            val errorMessage = "A database error occurred"
            ApiResult.Error(errorMessage)
        } catch (e: HttpRequestTimeoutException) {
            ApiResult.Error("The request timed out. Please check your connection and try again.")
        } catch (e: IOException) {
            ApiResult.Error("Unable to connect. Please check your internet connection.")
        } catch (e: ClientRequestException) {
            ApiResult.Error("Client error: ${e.response.status.description}")
        } catch (e: ServerResponseException) {
            ApiResult.Error("Server error: ${e.response.status.description}")
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.Error(e.message ?: "An unknown error occurred.")
        }
    }
}
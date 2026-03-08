package com.example.trackpoints.data.repository

import android.util.Log
import com.example.trackpoints.data.remote.ApiResult
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.ExperimentalTime

interface AuthRepository {
    suspend fun signIn(email: String, password: String): ApiResult<Unit>
    suspend fun signUp(email: String, password: String): ApiResult<String>
    suspend fun verifyOtp(type: OtpType.Email, email: String, code: String): ApiResult<Unit>
    suspend fun resendOtp(type: OtpType.Email, email: String): ApiResult<Unit>
    suspend fun checkIfEmailExists(email: String): ApiResult<Boolean>
    suspend fun sendPasswordReset(email: String): ApiResult<Unit>
    suspend fun confirmPasswordReset(newPassword: String): ApiResult<Unit>
    suspend fun signOut(): ApiResult<Unit>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}

class AuthRepositoryImpl(private val auth: Auth, private val postgrest: Postgrest) :
    AuthRepository {

    override suspend fun signIn(email: String, password: String): ApiResult<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            ApiResult.Success(Unit)
        } catch (e: RestException) {
            Log.d("SUPABASE_ERROR", "${e.message}")
            ApiResult.Error("Invalid email or password")
        } catch (e: Exception) {
            mapAuthException(e)
        }
    }

    override suspend fun signUp(email: String, password: String): ApiResult<String> {
        return try {
            val result = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("SIGNUP", result!!.id)
            ApiResult.Success(result.id)
        } catch (e: AuthRestException) {
            Log.d("SUPABASE_ERROR", "${e.message}")

            val errorMessage = "Create account failed"
            ApiResult.Error(errorMessage)
        } catch (e: Exception) {
            mapAuthException(e)
        }
    }

    override suspend fun verifyOtp(
        type: OtpType.Email,
        email: String,
        code: String
    ): ApiResult<Unit> {
        return try {
            auth.verifyEmailOtp(type = type, email = email, token = code)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.d("SUPABASE", e.message ?: "")
            if (e.message!!.contains("Token has expired or is invalid")) ApiResult.Error(message = "Token has expired or is invalid")
            else mapAuthException(e)
        }
    }

    override suspend fun resendOtp(type: OtpType.Email, email: String): ApiResult<Unit> {
        return try {
            auth.resendEmail(type, email)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Log.d("SUPABASE", "type=${type} email=${email}")
            Log.d("SUPABASE", e.message ?: "")
            mapAuthException(e)
        }
    }

    override suspend fun checkIfEmailExists(email: String): ApiResult<Boolean> {
        return try {
            val params = buildJsonObject {
                put("email_to_check", email)
            }

            val result: PostgrestResult = postgrest.rpc("check_if_email_exists", params)
            val userExists = result.data.toBooleanStrictOrNull() ?: false
            ApiResult.Success(userExists)
        } catch (e: Exception) {
            mapAuthException(e)
        }
    }

    override suspend fun sendPasswordReset(email: String): ApiResult<Unit> {
        return try {
            auth.resetPasswordForEmail(
                email = email,
            )
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            mapAuthException(e)
        }
    }

    override suspend fun confirmPasswordReset(newPassword: String): ApiResult<Unit> {
        return try {
            auth.updateUser {
                this.password = newPassword
            }
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            if (e.message!!.contains("New password should be different")) ApiResult.Error(message = "New password should be different from the old password")
            else if (e.message!!.contains("Token has expired or is invalid")) ApiResult.Error(
                message = "Token has expired or is invalid"
            )
            else mapAuthException(e)
        }
    }

    override suspend fun signOut(): ApiResult<Unit> {
        return try {
            auth.signOut()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            mapAuthException(e)
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun isLoggedIn(): Boolean {
        val user = auth.currentUserOrNull()
        val isEmailVerified = user?.emailConfirmedAt != null
        return isEmailVerified
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    fun mapAuthException(e: Throwable): ApiResult.Error {
        Log.d("SUPABASE_ERROR", "${e.message}")
        return when (e) {
            is HttpRequestTimeoutException -> {
                ApiResult.Error("Request timed out. Please check your connection.")
            }

            is ResponseException -> {
                ApiResult.Error("Server error: ${e.response.status.value}")
            }

            is IOException -> {
                ApiResult.Error("Network error. Please check your internet connection.")
            }

            else -> {
                e.printStackTrace()
                ApiResult.Error("An unknown error occurred.")
            }
        }
    }
}

package com.example.trackpoints.di

import com.example.trackpoints.presentation.auth.forgot_password.ForgotPasswordViewModel
import com.example.trackpoints.presentation.auth.sign_up.SignUpViewModel
import com.example.trackpoints.data.repository.AuthRepository
import com.example.trackpoints.data.repository.AuthRepositoryImpl
import com.example.trackpoints.data.repository.CommissionRepository
import com.example.trackpoints.data.repository.CommissionRepositoryImpl
import com.example.trackpoints.data.repository.MessageRepository
import com.example.trackpoints.data.repository.MessageRepositoryImpl
import com.example.trackpoints.data.repository.NotificationRepository
import com.example.trackpoints.data.repository.NotificationRepositoryImpl
import com.example.trackpoints.data.repository.UserRepository
import com.example.trackpoints.data.repository.UserRepositoryImpl
import com.example.trackpoints.presentation.auth.login.LoginViewModel
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.utils.NetworkConnectivityService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import okhttp3.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@OptIn(SupabaseInternal::class)
val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://kumpledfudlhalkfbler.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt1bXBsZWRmdWRsaGFsa2ZibGVyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI5OTU1NzUsImV4cCI6MjA3ODU3MTU3NX0.cSuXkswu2Kw6yOscvXvq1OVN1EfdcKg77N8zKEgkcaM"
        ) {
            install(Auth)
            install(Realtime)
            install(Postgrest)
            install(Storage)

            httpConfig {
                install(WebSockets)
                install(HttpTimeout) {
                    connectTimeoutMillis = 60_000L

//                    requestTimeoutMillis = 1L
                    requestTimeoutMillis = 60_000L

                    socketTimeoutMillis = 60_000L
                }
            }
        }
    }
    single { NetworkConnectivityService(androidContext()) }

    single<Postgrest> {
        get<SupabaseClient>().postgrest
    }

    single<Auth> {
        get<SupabaseClient>().auth
    }

    single<Realtime> {
        get<SupabaseClient>().realtime
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get(), get())
    }

    single<UserRepository> {
        UserRepositoryImpl(get(), get())
    }

    single<CommissionRepository> {
        CommissionRepositoryImpl(get(), get())
    }
    single<MessageRepository> {
        MessageRepositoryImpl(get(), get())
    }
    single<NotificationRepository> {
        NotificationRepositoryImpl(get(), get())
    }

    viewModel { SignUpViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
}
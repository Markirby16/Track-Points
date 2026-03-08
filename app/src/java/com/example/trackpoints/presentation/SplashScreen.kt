package com.example.trackpoints.presentation

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trackpoints.R
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.data.repository.AuthRepository
import com.example.trackpoints.presentation.main.FetchStatus
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainViewModel
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun SplashScreen(mainViewModel: MainViewModel, onLoaded: (Boolean) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )
    val auth: Auth = koinInject()
    val authRepository: AuthRepository = koinInject()

    LaunchedEffect(Unit) {
        auth.awaitInitialization()
        val user = mainViewModel.getCurrentUser()
        if (user != null) {
            mainViewModel.updateLastSeen()
            mainViewModel.handleIntent(MainIntent.FetchStats)
            mainViewModel.handleIntent(MainIntent.FetchCommissions)
            mainViewModel.handleIntent(MainIntent.FetchNotifications)

            if (user.role != UserRole.ADMIN)
                mainViewModel.handleIntent(MainIntent.FetchInbox)

            if (user.role == UserRole.CLIENT)
                mainViewModel.handleIntent(MainIntent.FetchFreelancers)
            else if (user.role == UserRole.ADMIN)
                mainViewModel.handleIntent(MainIntent.FetchUsers)
        } else {
            onLoaded(false)
        }
    }

    val state by mainViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.dataFetchStatus) {
        val user = mainViewModel.getCurrentUser()
        val status = state.dataFetchStatus
        val hasFetchedData =
            status.stats == FetchStatus.DONE && status.commissions == FetchStatus.DONE &&
                    status.notifications == FetchStatus.DONE && status.currentUser == FetchStatus.DONE

        if (user != null && hasFetchedData) {
            onLoaded(user.isApproved && authRepository.isLoggedIn())
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF3E2))
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.4f)) {
                Image(
                    painter = painterResource(id = R.drawable.trackpoints_logo),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .height(200.dp)
                        .scale(scale),
                )
            }
        }
    }
}


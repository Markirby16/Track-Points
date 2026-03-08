package com.example.trackpoints.presentation.shared

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.core.ui.ImageViewer
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.navigation.main.freelancerBottomBarItems
import com.example.trackpoints.presentation.client.PortfolioComponent
import com.example.trackpoints.presentation.main.MainEffect
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase

@Composable
fun ProfileScreenTab(
    state: MainState, onIntent: (MainIntent) -> Unit, onLogout: () -> Unit
) {
    var shouldUpdateProfilePic by remember { mutableStateOf(false) }
    var shouldUpdatePortfolioPics by remember { mutableStateOf(false) }
    var shouldUpdateProfileDetail by remember { mutableStateOf(false) }
    var shouldClearProfileDetail by remember { mutableStateOf(false) }
    var shouldEdit by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        ProfileContentTab(
            state,
            onIntent,
            shouldEdit,
            { shouldEdit = it },
            shouldUpdateProfilePic,
            shouldUpdateProfileDetail,
            shouldUpdatePortfolioPics,
            shouldClearProfileDetail,
            { shouldClearProfileDetail = it }
        )

        if (state.shouldLogout) {
            Alert(
                title = "Logout",
                message = "Are you sure you want to logout?",
                onDismissRequest = { onIntent(MainIntent.ShouldLogoutChanged(false)) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    onIntent(MainIntent.ShouldLogoutChanged(false))
                    onIntent(MainIntent.IsLoadingChanged(true))
                    onIntent(MainIntent.LogoutClicked)
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ShouldLogoutChanged(false)) })
        }
        if (state.actionError.contains("Are you sure you want to update your profile details?")) {
            Alert(
                title = "Update Profile Details",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    onIntent(MainIntent.IsLoadingChanged(true))
                    shouldUpdateProfileDetail = true
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Are you sure you want to discard changes?")) {
            Alert(
                title = "Discard Changes",
                message = state.actionError,
                onDismissRequest = {
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    shouldClearProfileDetail = true
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = {
                    onIntent(MainIntent.ActionErrorChanged(""))
                })
        } else if (state.actionError.contains("Are you sure you want to update your profile picture?")) {
            Alert(
                title = "Update Profile Picture",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    onIntent(MainIntent.IsLoadingChanged(true))
                    shouldUpdateProfilePic = true
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Are you sure you want to update your portfolio?")) {
            Alert(
                title = "Update Portfolio",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    onIntent(MainIntent.IsLoadingChanged(true))
                    shouldUpdatePortfolioPics = true
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Successfully updated profile details!")) {
            Alert(
                title = "Profile Updated",
                message = state.actionError,
                onDismissRequest = {
                    shouldUpdateProfileDetail = false
                    shouldEdit = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldUpdateProfileDetail = false
                    shouldEdit = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.contains("Successfully updated profile picture!")) {
            Alert(
                title = "Profile Updated",
                message = state.actionError,
                onDismissRequest = {
                    shouldUpdateProfilePic = false
                    shouldEdit = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldUpdateProfilePic = false
                    shouldEdit = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.contains("Successfully updated portfolio!")) {
            Alert(
                title = "Portfolio Updated",
                message = state.actionError,
                onDismissRequest = {
                    shouldUpdatePortfolioPics = false
                    shouldEdit = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldUpdatePortfolioPics = false
                    shouldEdit = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.contains("Invalid phone number, please try again")) {
            Alert(
                title = "Error",
                message = state.actionError,
                onDismissRequest = {
                    shouldClearProfileDetail = true
                    shouldUpdateProfileDetail = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldClearProfileDetail = true
                    shouldUpdateProfileDetail = false
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.isNotBlank()) {
            Alert(
                title = "Error",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Ok",
                onConfirmClicked = { onIntent(MainIntent.ActionErrorChanged("")) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContentTab(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    shouldEdit: Boolean,
    onShouldEditChanged: (Boolean) -> Unit,
    shouldUpdateProfilePic: Boolean,
    shouldUpdateProfileDetail: Boolean,
    shouldUpdatePortfolioPics: Boolean,
    shouldClearProfileDetail: Boolean,
    shouldClearProfileDetailChanged: (Boolean) -> Unit
) {
    val currentUser = state.currentUser!!
    var selectedImage by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(currentUser.phoneNumber ?: "") }
    var specialty by remember { mutableStateOf(currentUser.specialty ?: "") }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD3AD))
                .verticalScroll(rememberScrollState())
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            ProfileHeaderCard(
                currentUser,
                onIntent,
                { selectedImage = it },
                shouldEdit,
                onShouldEditChanged,
                shouldUpdateProfilePic,
                shouldUpdateProfileDetail,
                shouldClearProfileDetail,
                shouldClearProfileDetailChanged,
                shouldUpdatePortfolioPics,
                phoneNumber,
                { phoneNumber = it },
                specialty,
                { specialty = it },
                onSaveProfile = { phoneNumber, specialty ->
                    onIntent(
                        MainIntent.UpdateProfileDetail(
                            phoneNumber,
                            specialty
                        )
                    )
                }
            )
            AccountInfoCard(currentUser, onIntent)

            if (currentUser.role == UserRole.FREELANCER)
                PortfolioComponent(currentUser, { selectedImage = it }, shouldEdit)

            Spacer(modifier = Modifier.weight(0.5f))
            Button(
                onClick = { onIntent(MainIntent.ShouldLogoutChanged(true)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD0303).copy(0.5f)),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Log-out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        if (selectedImage.isNotBlank())
            ImageViewer(imageUrl = selectedImage, onDismiss = { selectedImage = "" })
    }
}

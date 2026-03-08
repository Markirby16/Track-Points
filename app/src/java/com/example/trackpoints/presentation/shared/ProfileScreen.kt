package com.example.trackpoints.presentation.shared

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.fallback
import coil3.request.placeholder
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.core.ui.CustomTextField
import com.example.trackpoints.core.ui.ImageViewer
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.navigation.main.freelancerBottomBarItems
import com.example.trackpoints.presentation.auth.sign_up.SignUpIntent
import com.example.trackpoints.presentation.client.PortfolioComponent
import com.example.trackpoints.presentation.main.MainEffect
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun ProfileScreen(
    viewModel: MainViewModel, onNavigateBack: () -> Unit, onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var shouldEdit by remember { mutableStateOf(false) }
    var shouldUpdateProfilePic by remember { mutableStateOf(false) }
    var shouldUpdatePortfolioPics by remember { mutableStateOf(false) }
    var shouldUpdateProfileDetail by remember { mutableStateOf(false) }
    var shouldClearProfileDetail by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MainEffect.NavigateProfile -> {}
                MainEffect.NavigateBack -> onNavigateBack()
                MainEffect.NavigateLogout -> onLogout()
                MainEffect.NavigateChat -> {}
                MainEffect.NavigateRequest -> {}
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ProfileContent(
            state,
            onIntent = viewModel::handleIntent,
            shouldEdit,
            { shouldEdit = it },
            shouldUpdateProfilePic,
            shouldUpdatePortfolioPics,
            shouldUpdateProfileDetail,
            shouldClearProfileDetail,
            { shouldClearProfileDetail = it }
        )

        if (state.isLoading) {
            LoadingOverlay()
        }
        if (state.shouldLogout) {
            Alert(
                title = "Logout",
                message = "Are you sure you want to logout?",
                onDismissRequest = { viewModel.handleIntent(MainIntent.ShouldLogoutChanged(false)) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    viewModel.handleIntent(MainIntent.ShouldLogoutChanged(false))
                    viewModel.handleIntent(MainIntent.IsLoadingChanged(true))
                    viewModel.handleIntent(MainIntent.LogoutClicked)
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { viewModel.handleIntent(MainIntent.ShouldLogoutChanged(false)) })
        }
        if (state.actionError.contains("Are you sure you want to update your profile details?")) {
            Alert(
                title = "Update Profile Details",
                message = state.actionError,
                onDismissRequest = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    viewModel.handleIntent(MainIntent.IsLoadingChanged(true))
                    shouldUpdateProfileDetail = true
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Are you sure you want to discard changes?")) {
            Alert(
                title = "Discard Changes",
                message = state.actionError,
                onDismissRequest = {
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    shouldClearProfileDetail = true
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = {
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                })
        } else if (state.actionError.contains("Are you sure you want to update your profile picture?")) {
            Alert(
                title = "Update Profile Picture",
                message = state.actionError,
                onDismissRequest = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    viewModel.handleIntent(MainIntent.IsLoadingChanged(true))
                    shouldUpdateProfilePic = true
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Are you sure you want to update your portfolio?")) {
            Alert(
                title = "Update Portfolio",
                message = state.actionError,
                onDismissRequest = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    viewModel.handleIntent(MainIntent.IsLoadingChanged(true))
                    shouldUpdatePortfolioPics = true
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Successfully updated profile details!")) {
            Alert(
                title = "Profile Updated",
                message = state.actionError,
                onDismissRequest = {
                    shouldEdit = false
                    shouldUpdateProfileDetail = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldEdit = false
                    shouldUpdateProfileDetail = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.contains("Successfully updated profile picture!")) {
            Alert(
                title = "Profile Updated",
                message = state.actionError,
                onDismissRequest = {
                    shouldEdit = false
                    shouldUpdateProfilePic = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldEdit = false
                    shouldUpdateProfilePic = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.contains("Successfully updated portfolio!")) {
            Alert(
                title = "Portfolio Updated",
                message = state.actionError,
                onDismissRequest = {
                    shouldEdit = false
                    shouldUpdatePortfolioPics = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldEdit = false
                    shouldUpdatePortfolioPics = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.contains("Invalid phone number, please try again")) {
            Alert(
                title = "Error",
                message = state.actionError,
                onDismissRequest = {
                    shouldClearProfileDetail = true
                    shouldUpdateProfileDetail = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldClearProfileDetail = true
                    shouldUpdateProfileDetail = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.isNotBlank()) {
            Alert(
                title = "Error",
                message = state.actionError,
                onDismissRequest = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Ok",
                onConfirmClicked = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    shouldEdit: Boolean,
    onShouldEditChanged: (Boolean) -> Unit,
    shouldUpdateProfilePic: Boolean,
    shouldUpdatePortfolioPics: Boolean,
    shouldUpdateProfileDetail: Boolean,
    shouldClearProfileDetail: Boolean,
    onShouldClearProfileDetailsChanged: (Boolean) -> Unit
) {
    val currentUser = state.currentUser!!
    var selectedImage by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(currentUser.phoneNumber ?: "") }
    var specialty by remember { mutableStateOf(currentUser.specialty ?: "") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF3E2)
                ),
                modifier = Modifier.dropShadow(
                    shape = RoundedCornerShape(0.dp), shadow = Shadow(
                        radius = 4.dp,
                        color = Color.Black.copy(0.25f),
                        offset = DpOffset(x = 0.dp, y = (4).dp)
                    )
                ),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Text(
                            text = "Profile Settings",
                            fontFamily = roboto,
                            fontSize = 20.sp,
                            color = Color(0xFF3F3F3F)
                        )
                        Text(
                            modifier = Modifier.offset(y = (-8).dp),
                            text = "Manage your account",
                            fontFamily = roboto,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Normal,
                            color = Color(0xFF3F3F3F)
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_icon),
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(top = 15.dp, bottom = 15.dp, start = 15.dp)
                            .clickable { onIntent(MainIntent.BackClicked) })
                },
            )
        },
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFD3AD))
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
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
                    onShouldClearProfileDetailsChanged,
                    shouldUpdatePortfolioPics,
                    phoneNumber,
                    { phoneNumber = it },
                    specialty,
                    { specialty = it },
                    onSaveProfile = { phoneNumber, specialty ->
                        onIntent(
                            MainIntent.UpdateProfileDetail(
                                phoneNumber, specialty
                            )
                        )
                    })
                AccountInfoCard(currentUser, onIntent)

                if (currentUser.role == UserRole.FREELANCER) PortfolioComponent(
                    currentUser,
                    { selectedImage = it },
                    shouldEdit
                )

                Spacer(modifier = Modifier.weight(0.5f))
                Button(
                    onClick = { onIntent(MainIntent.ShouldLogoutChanged(true)) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDD0303).copy(
                            0.5f
                        )
                    ),
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
}

val TextDark = Color(0xFF3E2723)
val TextLight = Color(0xFF8D6E63)

@Composable
fun ProfileHeaderCard(
    user: User,
    onIntent: (MainIntent) -> Unit,
    onImageClick: (String) -> Unit,
    shouldEdit: Boolean,
    onShouldEditChanged: (Boolean) -> Unit,
    shouldUpdateProfilePic: Boolean,
    shouldUpdateProfileDetails: Boolean,
    shouldClearProfileDetails: Boolean,
    onShouldClearProfileDetailsChanged: (Boolean) -> Unit,
    shouldUpdatePortfolioPics: Boolean,
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit,
    specialty: String = "",
    onSpecialtyChanged: (String) -> Unit,
    onSaveProfile: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var imageUris: List<Uri>? by remember { mutableStateOf(null) }

    LaunchedEffect(shouldUpdateProfilePic) {
        if (shouldUpdateProfilePic) imageUri?.let {
            scope.launch {
                onIntent(MainIntent.UpdateProfilePic(imageUri!!, context))
            }
        }
    }

    LaunchedEffect(shouldUpdatePortfolioPics) {
        if (shouldUpdatePortfolioPics) imageUris?.let {
            scope.launch {
                onIntent(MainIntent.UpdatePortfolioPics(imageUris!!, context))
            }
        }
    }

    LaunchedEffect(shouldUpdateProfileDetails) {
        if (shouldUpdateProfileDetails) onSaveProfile(phoneNumber, specialty)
    }

    if (shouldClearProfileDetails) {
        onPhoneNumberChanged(user.phoneNumber ?: "")
        onSpecialtyChanged(user.specialty ?: "")
        onShouldEditChanged(false)
        onShouldClearProfileDetailsChanged(false)
    }

    Surface(
        color = Color(0xFFFEF3E2),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    Modifier
                        .background(Color(0xFFFA812F), shape = CircleShape)
                        .size(130.dp)
                ) {
                    if (user.photo == null) Icon(
                        painterResource(R.drawable.profile_icon),
                        tint = Color(0xFF3F3F3F),
                        modifier = Modifier.size(130.dp),
                        contentDescription = null,
                    )
                    else AsyncImage(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .clickable { onImageClick(user.photo) },
                        model = ImageRequest.Builder(LocalContext.current).data(user.photo)
                            .placeholder(R.drawable.profile_icon)
                            .fallback(R.drawable.profile_icon).crossfade(true)
                            .listener(onStart = { request ->
                                Log.d(
                                    "IMAGE_LOAD", "Image started loading"
                                )
                            }, onError = { request, result ->
                                Log.e(
                                    "IMAGE_LOAD", "FAILED: ${result.throwable.message}"
                                )
                            }).build(),
                        contentDescription = "Chatmate Photo",
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.profile_icon)
                    )
                }
                val cropLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val resultUri = UCrop.getOutput(result.data!!)
                        if (resultUri != null) {
                            imageUri = resultUri
                            onIntent(MainIntent.ActionErrorChanged("Are you sure you want to update your profile picture?"))
                        }
                    }
                }
                val pickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        val mimeType = context.contentResolver.getType(uri)
                        val extension =
                            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                        val destinationUri = Uri.fromFile(
                            File(
                                context.cacheDir, "${UUID.randomUUID()}.$extension"
                            )
                        )

                        val uCrop = UCrop.of(uri, destinationUri).withAspectRatio(1f, 1f)
                            .withMaxResultSize(300, 300) // Optimized size
                            .withOptions(UCrop.Options().apply {
                                setCircleDimmedLayer(true)
                                setShowCropGrid(false)
                                setShowCropFrame(false)
                                setCompressionQuality(80)
                                setHideBottomControls(false)
                                setFreeStyleCropEnabled(false)
                            })

                        cropLauncher.launch(uCrop.getIntent(context))
                    }
                }
                Box(
                    modifier = Modifier
                        .offset(x = (-10).dp, y = (-2).dp)
                        .size(40.dp)
                        .background(Color(0xFFFA812F), CircleShape)
                        .border(2.dp, Color(0xFFFEF3E2), CircleShape)
                        .padding(6.dp)
                        .clickable {
                            pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                    Icon(
                        painterResource(R.drawable.camera_icon),
                        contentDescription = "Edit photo",
                        tint = Color(0xFF3F3F3F),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user.fullName.toTitleCase(),
                fontFamily = roboto,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F3F3F)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${user.role.toString().toTitleCase()} Account",
                fontFamily = roboto,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = Color(0xFF3F3F3F)
            )
            Spacer(Modifier.height(5.dp))
            if (!shouldEdit) Button(
                onClick = { onShouldEditChanged(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA812F)),
                shape = RoundedCornerShape(30.dp),
            ) {
                Text(
                    text = "Edit Profile",
                    fontFamily = roboto,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            if (shouldEdit)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    OutlinedTextField(
                        value = phoneNumber,
                        label = {
                            Text(
                                "Phone Number",
                                color = Color(0xFF3F3F3F),
                                fontSize = 14.sp,
                                fontFamily = roboto
                            )
                        },
                        onValueChange = onPhoneNumberChanged,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color(0xFF3F3F3F),
                            fontSize = 16.sp,
                            fontFamily = roboto,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color(0xFF3F3F3F),
                            unfocusedContainerColor = Color(0xFFFEF3E2),
                            focusedContainerColor = Color(0xFFFEF3E2),
                            unfocusedBorderColor = Color(0xFFDED4C5),
                            focusedBorderColor = Color(0xFFDED4C5),
                        ),
                        shape = RoundedCornerShape(6.dp),
                    )
                    if (user.role == UserRole.FREELANCER) {
                        OutlinedTextField(
                            value = specialty,
                            label = {
                                Text(
                                    "Specialty",
                                    color = Color(0xFF3F3F3F),
                                    fontSize = 14.sp,
                                    fontFamily = roboto
                                )
                            },
                            onValueChange = onSpecialtyChanged,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color(0xFF3F3F3F),
                                fontSize = 16.sp,
                                fontFamily = roboto,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color(0xFF3F3F3F),
                                unfocusedContainerColor = Color(0xFFFEF3E2),
                                focusedContainerColor = Color(0xFFFEF3E2),
                                unfocusedBorderColor = Color(0xFFDED4C5),
                                focusedBorderColor = Color(0xFFDED4C5),
                            ),
                            shape = RoundedCornerShape(6.dp),
                        )
                        val multiPhotoLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetMultipleContents()
                        ) { uris: List<Uri> ->
                            if (uris.isNotEmpty()) {
                                scope.launch {
                                    imageUris = uris
                                    onIntent(MainIntent.ActionErrorChanged("Are you sure you want to update your portfolio?"))
                                }
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .border(
                                    1.dp,
                                    color = Color(0xFFDED4C5),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    multiPhotoLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp, horizontal = 20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(R.drawable.upload_icon),
                                    "Upload Portfolio",
                                    tint = Color(0xFF3F3F3F),
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "Upload Photos\nfor Portfolio",
                                    fontFamily = roboto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {
                                if (phoneNumber.isNotBlank() || specialty.isNotBlank())
                                    onIntent(
                                        MainIntent.ActionErrorChanged("Are you sure you want to update your profile details?")
                                    )
                                onShouldEditChanged(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA812F)),
                            shape = RoundedCornerShape(30.dp),
                        ) {
                            Text(
                                text = "Save Profile",
                                fontFamily = roboto,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Button(
                            onClick = {
                                if (phoneNumber.isNotBlank() || specialty.isNotBlank()) {
                                    onIntent(MainIntent.ActionErrorChanged("Are you sure you want to discard changes?"))
                                } else
                                    onShouldEditChanged(false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA812F)),
                            shape = RoundedCornerShape(30.dp),
                        ) {
                            Text(
                                text = "Close Edit",
                                fontFamily = roboto,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun AccountInfoCard(
    user: User, onIntent: (MainIntent) -> Unit
) {
    Surface(
        color = Color(0xFFFEF3E2),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 24.dp)) {
            Text(
                text = "Account Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(start = 24.dp, bottom = 20.dp)
            )

            HorizontalDivider(color = Color(0xFFDED4C5))

            InfoRow(
                icon = R.drawable.email_icon,
                iconBgColor = Color(0xFFC1BEDE),
                iconTint = Color(0xFF1700E6),
                label = "Email Account",
                value = user.email
            )

            HorizontalDivider(color = Color(0xFFDED4C5))
            InfoRow(
                icon = R.drawable.phone_icon,
                iconBgColor = Color(0xFFCAF5BF),
                iconTint = Color(0xFF389C1F),
                label = "Phone Number",
                value = "+63 ${user.phoneNumber ?: "N/A"}"
            )
            HorizontalDivider(color = Color(0xFFDED4C5))

            InfoRow(
                icon = R.drawable.calendar_icon,
                iconBgColor = Color(0xFFE5C3E8),
                iconTint = Color(0xFFA649AE),
                label = "Member since",
                value = user.createdAt.toDashDateString()
            )
        }
    }
}

@Composable
fun InfoRow(
    @DrawableRes icon: Int,
    iconBgColor: Color,
    iconTint: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label, fontSize = 12.sp, color = TextLight
            )
            Text(
                text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark
            )
        }
    }
}


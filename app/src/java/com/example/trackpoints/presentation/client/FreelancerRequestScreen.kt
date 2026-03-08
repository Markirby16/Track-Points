package com.example.trackpoints.presentation.client

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.fallback
import coil3.request.placeholder
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.core.ui.ImageViewer
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.CommissionRequest
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.presentation.main.MainEffect
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.toTitleCase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun FreelancerRequestScreen(
    viewModel: MainViewModel, onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var shouldSubmitCommission by remember { mutableStateOf(false) }
    var shouldClearFields by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MainEffect.NavigateProfile -> {}
                MainEffect.NavigateBack -> onNavigateBack()
                MainEffect.NavigateLogout -> {}
                MainEffect.NavigateChat -> {}
                MainEffect.NavigateRequest -> {}
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        FreelancerRequestContent(
            state,
            onIntent = viewModel::handleIntent,
            shouldSubmitCommission,
            shouldClearFields,
            { shouldClearFields = it }
        )

        if (state.isLoading) {
            LoadingOverlay()
        }
        if (state.actionError.contains("Are you sure you want to submit this commission request?")) {
            Alert(
                title = "Request Commission",
                message = state.actionError,
                onDismissRequest = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    viewModel.handleIntent(MainIntent.IsLoadingChanged(true))
                    shouldSubmitCommission = true
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Successfully submitted commission request!")) {
            Alert(
                title = "Submitted Commission",
                message = state.actionError,
                onDismissRequest = {
                    shouldClearFields = true
                    shouldSubmitCommission = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldClearFields = true
                    shouldSubmitCommission = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
            )
        } else if (state.actionError.isNotBlank()) {
            Alert(
                title = "Error",
                message = state.actionError,
                onDismissRequest = {
                    shouldSubmitCommission = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged(""))
                },
                confirmButtonText = "Ok",
                onConfirmClicked = {
                    shouldSubmitCommission = false
                    viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreelancerRequestContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    shouldSubmitCommission: Boolean,
    shouldClearFields: Boolean,
    onShouldClearFieldsChanged: (Boolean) -> Unit
) {
    val currentUser = state.currentUser!!
    val freelancer = state.currentFreelancerToRequest!!

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
                            text = "Find Freelancers",
                            fontFamily = roboto,
                            fontSize = 20.sp,
                            color = Color(0xFF3F3F3F)
                        )
                        Text(
                            modifier = Modifier.offset(y = (-8).dp),
                            text = "Create a Project Request",
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
        var selectedImage by remember { mutableStateOf("") }

        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFD3AD))
                    .padding(paddingValues)
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    PortfolioComponent(freelancer, { selectedImage = it })
                    Spacer(Modifier.height(30.dp))
                }
                item {
                    CommissionRequestComponent(
                        currentUser,
                        freelancer,
                        onIntent,
                        shouldSubmitCommission,
                        shouldClearFields,
                        onShouldClearFieldsChanged
                    )
                    Spacer(Modifier.height(30.dp))
                }
            }
            if (selectedImage.isNotBlank())
                ImageViewer(imageUrl = selectedImage, onDismiss = { selectedImage = "" })
        }
    }
}

@Composable
fun PortfolioComponent(
    user: User,
    onImageClick: (String) -> Unit,
    shouldEdit: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = Color(0xFFF5EBDA)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.photo == null)
                    Icon(
                        painterResource(R.drawable.profile_icon),
                        null,
                        Modifier
                            .size(110.dp),
                        tint = Color(0xFFFA812F)
                    )
                else
                    AsyncImage(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(shape = CircleShape)
                            .clickable { onImageClick(user.photo) },
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photo)
                            .placeholder(R.drawable.profile_icon_orange)
                            .fallback(R.drawable.profile_icon_orange)
                            .crossfade(true).listener(
                                onStart = { request ->
                                    Log.d(
                                        "IMAGE_LOAD",
                                        "Image started loading"
                                    )
                                },
                                onError = { request, result ->
                                    Log.e(
                                        "IMAGE_LOAD", "FAILED: ${result.throwable.message}"
                                    )
                                }).build(),
                        contentDescription = "Freelancer Photo",
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.profile_icon_orange)
                    )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        user.fullName.toTitleCase(),
                        fontFamily = roboto,
                        color = Color(0xFF3F3F3F),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp
                    )
                    Text(
                        "Specialty (${user.specialty ?: "N/A"})",
                        color = Color(0xFF3F3F3F),
                        fontFamily = roboto,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Light,
                        lineHeight = 14.sp,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.star_icon),
                            "Rating",
                            tint = Color(0xFFE2AD00),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            "${user.rating ?: 5.0}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F3F3F),
                            fontFamily = roboto,
                        )
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            Text(
                "Portfolio",
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 5.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (user.portfolio == null || user.portfolio.isEmpty())
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.49f)
                                .aspectRatio(1f)
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.image_icon),
                                "Portfolio Image",
                                tint = Color(0xFF3F3F3F),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                else
                    user.portfolio.forEach { image ->
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth(0.49f)
                                .aspectRatio(1f)
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onImageClick(image) },
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image)
                                .placeholder(R.drawable.image_icon)
                                .fallback(R.drawable.image_icon)
                                .crossfade(true).listener(
                                    onStart = { request ->
                                        Log.d(
                                            "IMAGE_LOAD",
                                            "Image started loading"
                                        )
                                    },
                                    onError = { request, result ->
                                        Log.e(
                                            "IMAGE_LOAD",
                                            "FAILED: ${result.throwable.message}"
                                        )
                                    }).build(),
                            contentDescription = "Sample Portfolio Photo",
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.image_icon)
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionRequestComponent(
    currentUser: User,
    freelancer: User,
    onIntent: (MainIntent) -> Unit,
    shouldSubmitCommission: Boolean,
    shouldClearFields: Boolean,
    onShouldClearFieldsChanged: (Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var pointsText by remember { mutableStateOf("1000") }
    val pointsInt = pointsText.toIntOrNull() ?: 0
    val hasFilledOut =
        name.isNotBlank() && description.isNotBlank() && deadline.isNotBlank() && pointsInt > 0

    LaunchedEffect(shouldSubmitCommission) {
        if (shouldSubmitCommission) {
            Log.d("SUBMIT", "SUBMIT RAN")
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val localDate = LocalDate.parse(deadline, formatter)

            val phZoneId = ZoneId.of("Asia/Manila")
            val zonedDeadlineDateTime = localDate.atStartOfDay(phZoneId)

            val commissionRequest = CommissionRequest(
                name,
                currentUser.id,
                freelancer.id,
                CommissionStatus.PENDING,
                pointsInt,
                description,
                zonedDeadlineDateTime.toInstant(),
            )
            onIntent(MainIntent.SubmitRequest(commissionRequest))
        }
    }

    if (shouldClearFields) {
        name = ""
        description = ""
        deadline = ""
        pointsText = ""
        onShouldClearFieldsChanged(false)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = Color(0xFFF5EBDA)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.details_icon),
                    "Details",
                    tint = Color(0xFFFA812F)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Commission Request Details",
                    fontFamily = roboto,
                    color = Color(0xFF3F3F3F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            CustomField(value = name, onValueChange = { name = it }, label = "Project Name*")
            Spacer(Modifier.height(12.dp))
            CustomField(
                value = description,
                onValueChange = { description = it },
                label = "Project Description*",
                isMultiLine = true
            )

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    DatePickerField(selectedDate = deadline, onValueChange = { deadline = it })
                }
                Box(Modifier.weight(1f)) {
                    CustomField(
                        value = pointsText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                pointsText = newValue
                            }
                        },
                        label = "Points*",
                        isNumeric = true
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    if (hasFilledOut) {
                        onIntent(MainIntent.ActionErrorChanged("Are you sure you want to submit this commission request?"))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA812F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painterResource(R.drawable.send_icon),
                    "Send Commission Request",
                    Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Submit Commission Request",
                    fontFamily = roboto,
                    color = Color(0xFFFEF3E2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CustomField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isMultiLine: Boolean = false,
    isNumeric: Boolean = false
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 15.sp,
            color = Color(0xFF3F3F3F),
            fontFamily = roboto,
            fontWeight = FontWeight.Thin
        )
        Spacer(Modifier.height(6.dp))
        Surface(Modifier.fillMaxWidth(), tonalElevation = 8.dp, shadowElevation = 8.dp) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isMultiLine) Modifier
                        else Modifier.height(50.dp)
                    ),
                textStyle = TextStyle(
                    color = Color(0xFF3F3F3F),
                    fontFamily = roboto,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(6.dp),
                singleLine = !isMultiLine,
                minLines = if (isMultiLine) 6 else 1,
                maxLines = if (isMultiLine) 6 else 1,
                keyboardOptions = if (isNumeric) KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ) else KeyboardOptions.Default,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(selectedDate: String, onValueChange: (String) -> Unit) {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayMillis = calendar.timeInMillis

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= LocalDate.now().year
            }
        }
    )

    var showDatePicker by remember { mutableStateOf(false) }
//    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        onValueChange(
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(date))
                        )
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column {
        Text(
            "Deadline*",
            fontSize = 15.sp,
            color = Color(0xFF3F3F3F),
            fontFamily = roboto,
            fontWeight = FontWeight.Thin
        )
        OutlinedTextField(
            value = selectedDate,
            onValueChange = onValueChange,
            label = { Text("Select Date") },
            readOnly = true,
            enabled = false,
            textStyle = TextStyle(
                color = Color(0xFF3F3F3F),
                fontFamily = roboto,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
            )
        )
    }
}

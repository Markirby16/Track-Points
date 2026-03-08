package com.example.trackpoints.presentation.admin

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.fallback
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.data.model.User
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase

@Composable
fun UserRequestsScreen(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
) {
    val userRequests = state.users.filter { !it.isApproved && !it.isRejected }
    var selectedRequest: User? by remember { mutableStateOf(null) }

    Box {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD3AD))
                .padding(horizontal = 24.dp)
                .padding(top = 30.dp, bottom = 30.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(15.dp),
                color = Color(0xFFFEF3E2),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "New User Requests",
                        fontFamily = roboto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3F3F),
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Spacer(Modifier.height(15.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        items(userRequests, { user -> user.id }) { user ->
                            PendingUserCard(user, onIntent, { selectedRequest = it })
                        }
                    }
                }
            }
        }
        if (state.actionError.contains("Are you sure")) {
            Alert(
                title = "Confirm Action",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    onIntent(MainIntent.IsLoadingChanged(true))
                    val shouldMarkRequest = state.actionError.contains("approve")
                    onIntent(MainIntent.MarkUserRequest(shouldMarkRequest, selectedRequest!!.id))
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Successfully")) {
            Alert(
                title = "User Request",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Ok",
                onConfirmClicked = { onIntent(MainIntent.ActionErrorChanged("")) },
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

@Composable
fun PendingUserCard(
    user: User,
    onIntent: (MainIntent) -> Unit,
    onMarkRequest: (User?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3E2)),
        border = BorderStroke(1.dp, Color(0xFFDED4C5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, end = 10.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Row(
                    Modifier
                        .background(Color(0xFFECDEAE), shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.pending_icon),
                        contentDescription = "Pending User",
                        tint = Color(0xFFBF9300),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Pending",
                        fontFamily = roboto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFFBF9300),
                    )
                }
            }
            Column(
                Modifier
                    .padding(vertical = 15.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement
                    .spacedBy(5.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (user.photo == null)
                        Icon(
                            painterResource(R.drawable.profile_icon),
                            contentDescription = null,
                            modifier = Modifier.size(53.dp),
                            tint = Color(0xFFFA812F)
                        ) else
                        AsyncImage(
                            modifier = Modifier
                                .size(53.dp)
                                .clip(CircleShape),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.photo)
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
                            contentDescription = "Active User Photo",
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.profile_icon_orange)
                        )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = user.fullName.toTitleCase(),
                        fontFamily = roboto,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3F3F),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
                Text(
                    text = user.role.toString().toTitleCase(),
                    fontFamily = roboto,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF3F3F3F),
                )
                Text(
                    text = "Date: ${user.createdAt.toDashDateString()}",
                    fontFamily = roboto,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF3F3F3F),
                )
                Row(
                    Modifier
                        .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val role = user.role.toString().toTitleCase()
                    Button(
                        onClick = {
                            onMarkRequest(user)
                            onIntent(MainIntent.ActionErrorChanged("Are you sure you want to approve this user as a $role?"))
                        },
                        modifier = Modifier.weight(0.48f),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, Color(0xFF389C1F)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCAF5BF)
                        )
                    ) {
                        Text(
                            text = "✔ Approve",
                            fontFamily = roboto,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF389C1F)
                        )
                    }
                    Spacer(Modifier.weight(0.04f))
                    Button(
                        onClick = {
                            onMarkRequest(user)
                            onIntent(MainIntent.ActionErrorChanged("Are you sure you want to reject this user as a $role?"))
                        },
                        modifier = Modifier.weight(0.48f),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, Color(0xFFCE2323)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF99898)
                        )
                    ) {
                        Text(
                            text = "✖ Reject",
                            fontFamily = roboto,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFCE2323)
                        )
                    }
                }
            }
        }
    }
}

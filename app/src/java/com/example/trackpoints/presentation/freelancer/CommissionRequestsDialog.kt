package com.example.trackpoints.presentation.freelancer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.toAbbreviatedString
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase

@Composable
fun CommissionRequestsDialog(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    val commissions =
        state.commissions.filter { it.status == CommissionStatus.PENDING }
    var selectedRequest: Commission? by remember { mutableStateOf(null) }

    Box {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(15.dp),
                color = Color(0xFFFEF3E2),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Box {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .padding(start = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Requests",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F3F3F)
                            )
                            IconButton(onClick = onDismissRequest) {
                                Icon(
                                    painter = painterResource(R.drawable.close_icon),
                                    contentDescription = "Close",
                                    tint = Color(0xFF3F3F3F),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(commissions, { it.id }) { commission ->
                                ProjectRequest(
                                    commission = commission,
                                    onIntent,
                                    { selectedRequest = it })
                            }
                        }
                    }
                    if (state.isLoading) {
                        LoadingOverlay()
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
                    val shouldMarkRequest = state.actionError.contains("accept")
                    onIntent(
                        MainIntent.MarkCommissionRequest(
                            shouldMarkRequest,
                            selectedRequest!!.id
                        )
                    )
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Successfully")) {
            Alert(
                title = "Commission Request",
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
fun ProjectRequest(
    commission: Commission,
    onIntent: (MainIntent) -> Unit,
    onMarkRequest: (Commission?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC089)),
        border = BorderStroke(1.dp, Color(0xFFFA812F)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(vertical = 25.dp, horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = commission.name.toTitleCase(),
                    fontFamily = roboto,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF3F3F3F),
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
                Row {
                    Icon(
                        painterResource(R.drawable.points_icon),
                        contentDescription = "Points",
                        tint = Color(0xFFFA812F),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = commission.points.toAbbreviatedString(),
                        fontFamily = roboto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFA812F)
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = "From: ${commission.client.fullName.toTitleCase()}",
                fontFamily = roboto,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light,
                color = Color(0xFF3F3F3F)
            )
            Spacer(Modifier.height(15.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier
                        .weight(0.48f)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color(0xFFFEF3E2))
                        .border(1.dp, Color(0xFFFA812F), shape = RoundedCornerShape(25.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Deadline:\n")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            ) {
                                append(commission.dueDate.toDashDateString())
                            }
                        },
                        fontFamily = roboto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF3F3F3F)
                    )
                }
                Spacer(Modifier.weight(0.04f))
                Box(
                    Modifier
                        .weight(0.48f)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color(0xFFFEF3E2))
                        .border(1.dp, Color(0xFFFA812F), shape = RoundedCornerShape(25.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Request ID:\n")
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            ) {
                                append("#${commission.id}")
                            }
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = roboto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF3F3F3F)
                    )
                }
            }
            Spacer(Modifier.height(15.dp))
            Text(
                text = "Project Description",
                fontFamily = roboto,
                fontSize = 15.sp,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF3F3F3F)
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = commission.description,
                fontFamily = roboto,
                fontSize = 13.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light,
                color = Color(0xFF3F3F3F)
            )
            Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        onMarkRequest(commission)
                        onIntent(MainIntent.ActionErrorChanged("Are you sure you want to accept this commission request?"))
                    },
                    modifier = Modifier.weight(0.48f),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFF389C1F)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCAF5BF)
                    )
                ) {
                    Text(
                        text = "✔ Accept",
                        fontFamily = roboto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF389C1F)
                    )
                }
                Spacer(Modifier.weight(0.04f))
                Button(
                    onClick = {
                        onMarkRequest(commission)
                        onIntent(MainIntent.ActionErrorChanged("Are you sure you want to decline this commission request?"))
                    },
                    modifier = Modifier.weight(0.48f),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFFCE2323)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF99898)
                    )
                ) {
                    Text(
                        text = "✖ Decline",
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

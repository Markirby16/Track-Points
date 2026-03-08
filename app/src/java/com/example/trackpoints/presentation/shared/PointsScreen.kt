package com.example.trackpoints.presentation.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.utils.toAbbreviatedString
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase

@Composable
fun PointsScreen(
    state: MainState, onIntent: (MainIntent) -> Unit
) {
    var selectedCommission: Commission? by remember { mutableStateOf(null) }
    var rating by remember { mutableIntStateOf(0) }
    var shouldRateFreelancer by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        PointsContent(
            state = state, onIntent = onIntent, shouldRateFreelancer,
            { shouldRateFreelancer = it },
            selectedCommission,
            rating,
            { rating = it },
            {
                selectedCommission = it
                onIntent(MainIntent.ActionErrorChanged("Are you sure you want to mark this commission as done and send the points?"))
            }
        )

        if (state.actionError.contains("Are you sure you want to mark this commission as done and send the points?")) {
            Alert(
                title = "Confirm Action",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    shouldRateFreelancer = true
                    onIntent(MainIntent.ActionErrorChanged(""))
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
        } else if (state.actionError.contains("Successfully sent points to")) {
            Alert(
                title = "Project Completed",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Ok",
                onConfirmClicked = { onIntent(MainIntent.ActionErrorChanged("")) },
            )
        } else if (state.actionError.contains("Are you sure you want to send this rating?")) {
            Alert(
                title = "Confirm Action",
                message = state.actionError,
                onDismissRequest = { onIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Confirm",
                onConfirmClicked = {
                    onIntent(MainIntent.IsLoadingChanged(true))
                    onIntent(MainIntent.SendPointsAndRating(selectedCommission!!, rating))
                    onIntent(MainIntent.ActionErrorChanged(""))
                    shouldRateFreelancer = false
                },
                cancelButtonText = "Cancel",
                onCancelClicked = { onIntent(MainIntent.ActionErrorChanged("")) })
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
fun PointsContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    shouldRateFreelancer: Boolean,
    onShouldRateFreelancerChanged: (Boolean) -> Unit,
    selectedCommission: Commission?,
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    onSelectedCommission: (Commission) -> Unit
) {
    val currentUser = state.currentUser!!
    val commissions = state.commissions
    val pointsSent = commissions.filter { it.status == CommissionStatus.DONE }.sumOf { it.points }
    val pointsInProgress =
        commissions.filter { it.status == CommissionStatus.IN_PROGRESS }.sumOf { it.points }

    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFD3AD))
                .padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatBox(
                        title = "Points Sent",
                        points = pointsSent.toAbbreviatedString(),
                        bgColor = Color(0xFFC1BEDE),
                        borderColor = Color(0xFF1700E6)
                    )
                    StatBox(
                        title = "In Progress",
                        points = pointsInProgress.toAbbreviatedString(),
                        bgColor = Color(0xFFFFC089),
                        borderColor = Color(0xFFFA812F)
                    )
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    border = BorderStroke(1.dp, Color(0xFFDED4C5)),
                    shape = RoundedCornerShape(15.dp),
                    color = Color(0xFFFEF3E2),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 35.dp, horizontal = 25.dp)
                    ) {
                        Text(
                            "Completed Points Sent",
                            fontFamily = roboto,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3F3F3F)
                        )
                        for (commission in commissions.filter { it.status == CommissionStatus.DONE }) {
                            Spacer(Modifier.height(8.dp))
                            CommissionItem(commission, currentUser.role)
                            Spacer(Modifier.height(8.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFDED4C5))
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Pending Points",
                            fontFamily = roboto,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3F3F3F)
                        )
                        for (commission in commissions.filter { it.status == CommissionStatus.IN_PROGRESS }) {
                            Spacer(Modifier.height(8.dp))
                            CommissionItem(commission, currentUser.role, onSelectedCommission)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
        if (shouldRateFreelancer && selectedCommission != null) RatingDialog(
            selectedCommission,
            rating,
            { onShouldRateFreelancerChanged(false) },
            onRatingChanged,
            { onIntent(MainIntent.ActionErrorChanged("Are you sure you want to send this rating?")) })

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingDialog(
    commission: Commission,
    rating: Int,
    onDismiss: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFD3AD))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rate your experience",
                        color = Color.Black,
                        fontFamily = AppFonts.robotoCondensed,
                        fontSize = 16.3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "How was ${
                            commission.freelancer.fullName.toTitleCase().split(" ")
                                .firstOrNull() ?: ""
                        } work on '${commission.name}'?",
                        color = Color.Black,
                        fontFamily = AppFonts.roboto,
                        fontSize = 12.sp,
                        lineHeight = 17.3.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            val isSelected = starIndex <= rating

                            Icon(
                                painterResource(if (isSelected) R.drawable.filled_star_icon else R.drawable.outline_star_icon),
                                contentDescription = "Star Rating",
                                tint = if (isSelected) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onRatingChanged(starIndex) })
                        }
                    }
                }
                HorizontalDivider(
                    color = Color(0xFF261A36).copy(alpha = 0.6f), thickness = 0.81.dp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDismiss()
                            }
                            .padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cancel",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontFamily = AppFonts.roboto,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = Color(0xFF261A36).copy(alpha = 0.6f),
                        thickness = 0.81.dp
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onConfirm()
                            }
                            .padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        val buttonColor = Color(0xFFEE101A)
                        Text(
                            text = "Confirm",
                            color = buttonColor,
                            fontSize = 15.sp,
                            fontFamily = AppFonts.roboto,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommissionItem(commission: Commission, role: UserRole, onClick: (Commission) -> Unit = {}) {
    var description = ""
    if (role == UserRole.CLIENT) {
        description =
            if (commission.status == CommissionStatus.DONE) "Sent to (${commission.freelancer.fullName.toTitleCase()})" else "To (${commission.freelancer.fullName.toTitleCase()})"
    } else if (role == UserRole.FREELANCER) {
        description =
            if (commission.status == CommissionStatus.DONE) "Sent by (${commission.client.fullName.toTitleCase()})" else "From (${commission.client.fullName.toTitleCase()})"
    }

    Column {
        Row(verticalAlignment = Alignment.Top) {
            if (commission.status == CommissionStatus.DONE) Box(
                Modifier
                    .size(35.dp)
                    .background(Color(0xFFCAF5BF), shape = CircleShape)
                    .align(Alignment.Top), contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.done_icon),
                    "Project Done",
                    modifier = Modifier.size(25.dp),
                    tint = Color(0xFF389C1F)
                )
            }
            else if (commission.status == CommissionStatus.IN_PROGRESS) Box(
                Modifier
                    .size(35.dp)
                    .background(Color(0xFFFFC089), shape = CircleShape)
                    .align(Alignment.Top), contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.card_icon),
                    "Project In Progress",
                    modifier = Modifier.size(25.dp),
                    tint = Color(0xFFFA812F)
                )
            }
            Spacer(Modifier.width(5.dp))
            Column(Modifier.fillMaxWidth(0.7f)) {
                Text(
                    commission.name.toTitleCase(),
                    fontFamily = roboto,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF3F3F3F)
                )
                Text(
                    description,
                    fontFamily = roboto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF3F3F3F)
                )
                if (commission.status == CommissionStatus.DONE) Text(
                    commission.datePaid.toDashDateString(),
                    fontFamily = roboto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF3F3F3F)
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                painterResource(R.drawable.points_icon),
                contentDescription = "Points",
                tint = if (commission.status == CommissionStatus.DONE) Color(0xFF3F3F3F)
                else Color(0xFFFA812F),
                modifier = Modifier.size(25.dp)
            )
            Text(
                commission.points.toAbbreviatedString(),
                fontFamily = roboto,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (commission.status == CommissionStatus.DONE) Color(0xFF3F3F3F)
                else Color(0xFFFA812F),
            )
        }
        if (commission.status == CommissionStatus.IN_PROGRESS) Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = if (role == UserRole.CLIENT) {
                { onClick(commission) }
            } else {
                { }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB1B1B1)
            ),
            border = BorderStroke(1.dp, Color(0xFF3F3F3F)),
            shape = RoundedCornerShape(50)) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                if (role == UserRole.CLIENT) "Send Points" else "Waiting for Client",
                fontSize = 16.sp,
                fontFamily = roboto,
                color = Color(0xFF3F3F3F),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatBox(title: String, points: String, bgColor: Color, borderColor: Color) {
    Column(
        Modifier
            .background(bgColor, shape = RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, shape = RoundedCornerShape(10.dp))
            .padding(vertical = 30.dp, horizontal = 35.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontFamily = roboto,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = borderColor
        )
        Text(
            text = points.uppercase(),
            fontFamily = roboto,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = borderColor
        )
        Icon(
            painterResource(R.drawable.points_icon),
            contentDescription = "Points",
            tint = borderColor,
            modifier = Modifier.size(40.dp, 60.dp)
        )
    }
}

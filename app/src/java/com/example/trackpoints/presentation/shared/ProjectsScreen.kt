package com.example.trackpoints.presentation.shared

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.displayName
import com.example.trackpoints.utils.toAbbreviatedString
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase

@Composable
fun ProjectsScreen(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onNavigateMessages: (Commission) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        ProjectsContent(
            state = state,
            onIntent = onIntent,
            onNavigateMessages = onNavigateMessages
        )
    }
}

@Composable
fun ProjectsContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onNavigateMessages: (Commission) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val currentUser = state.currentUser!!
    val commissions = if (currentUser.role == UserRole.CLIENT)
        state.commissions
    else
        state.commissions.filter { it.status != CommissionStatus.PENDING && it.status != CommissionStatus.REJECTED }
    val requests =
        state.commissions.filter { it.status == CommissionStatus.PENDING && it.freelancer.id == currentUser.id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD3AD))
            .padding(24.dp),
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
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Requests",
                        fontFamily = roboto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3F3F)
                    )
                    if (currentUser.role == UserRole.FREELANCER)
                        TextButton(
                            onClick = {
                                onIntent(MainIntent.ShouldShowCommissionRequestsChanged(true))
                            }
                        ) {
                            Box {
                                Text(
                                    text = "View New\nRequests >",
                                    fontFamily = roboto,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Thin,
                                    color = Color(0xFFFA812F)
                                )
                                if (requests.count() > 0)
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDD0303))
                                            .align(Alignment.TopEnd)
                                    )
                            }
                        }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = roboto,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color(0xFFF3F3F3)
                    ),
                    placeholder = {
                        Text(
                            "Search Projects..",
                            fontFamily = roboto,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            color = Color(0xFFA2A2A2)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.search_icon),
                            contentDescription = "Search",
                            tint = Color(0xFFA2A2A2),
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF3F3F3F).copy(0.16f),
                        focusedContainerColor = Color(0xFF3F3F3F).copy(0.16f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    val role = currentUser.role
                    val filteredCommissions =
                        if (searchText.isNotBlank()) commissions.filter {
                            val name = it.name.lowercase()
                            val nameOfUser =
                                (if (role == UserRole.CLIENT) it.freelancer else it.client).fullName.lowercase()
                            val date = it.dueDate.toDashDateString()
                            val status = it.status.displayName.lowercase()
                            val points = it.points.toString()

                            name.contains(searchText) || nameOfUser.contains(searchText) ||
                                    date.contains(searchText) || status.contains(searchText) ||
                                    points.contains(searchText)
                        } else commissions

                    items(
                        filteredCommissions,
                        { commission -> commission.id }) { commission ->
                        CommissionCard(
                            commission,
                            currentUser.role,
                            { onNavigateMessages(commission) })
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }
            }
        }
    }
}

@Composable
fun CommissionCard(commission: Commission, role: UserRole, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3E2)),
        border = BorderStroke(1.dp, Color(0xFFDED4C5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.padding(vertical = 15.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement
                .spacedBy(5.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    Modifier.fillMaxWidth(0.7f),
                ) {
                    Text(
                        text = commission.name.toTitleCase(),
                        fontFamily = roboto,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3F3F)
                    )
                    var bgColor = Color(0xFFECDEAE)
                    var contentColor = Color(0xFFBF9300)
                    var icon = R.drawable.pending_icon

                    if (commission.status == CommissionStatus.IN_PROGRESS) {
                        bgColor = Color(0xFFC1BEDE)
                        contentColor = Color(0xFF1700E6)
                        icon = R.drawable.info_icon
                    } else if (commission.status == CommissionStatus.DONE) {
                        bgColor = Color(0xFFCAF5BF)
                        contentColor = Color(0xFF389C1F)
                        icon = R.drawable.done_icon
                    } else if (commission.status == CommissionStatus.REJECTED) {
                        bgColor = Color(0xFFFFA7A7)
                        contentColor = Color(0xFFFF0000)
                        icon = R.drawable.info_icon
                    }

                    Spacer(Modifier.height(5.dp))
                    Row(
                        Modifier
                            .background(bgColor, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(icon),
                            contentDescription = commission.status.displayName,
                            tint = contentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = commission.status.displayName,
                            fontFamily = roboto,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            color = contentColor
                        )
                    }
                }
                Row {
                    Icon(
                        painterResource(R.drawable.points_icon),
                        contentDescription = "Points",
                        tint = Color(0xFFFA812F),
                        modifier = Modifier.size(25.dp)
                    )
                    Text(
                        text = commission.points.toAbbreviatedString(),
                        fontFamily = roboto,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFA812F)
                    )
                }
            }
            Text(
                text = commission.description,
                fontFamily = roboto,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                fontWeight = FontWeight.Thin,
                color = Color(0xFF3F3F3F)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (role == UserRole.CLIENT) commission.freelancer.fullName.toTitleCase() else commission.client.fullName.toTitleCase(),
                    fontFamily = roboto,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth(0.55f),
                    color = Color(0xFF3F3F3F)
                )
                Text(
                    text = "Due Date:\n${commission.dueDate.toDashDateString()}",
                    fontFamily = roboto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    color = Color(0xFF3F3F3F)
                )
            }
        }
    }
}

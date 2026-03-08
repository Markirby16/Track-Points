package com.example.trackpoints.presentation.shared

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.fallback
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.NotificationItem
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.displayName
import com.example.trackpoints.utils.isOnline
import com.example.trackpoints.utils.toAbbreviatedString
import com.example.trackpoints.utils.toDashDateString
import com.example.trackpoints.utils.toTitleCase

@Composable
fun HomeScreen(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onNavigateProjects: () -> Unit,
    onNavigateMessages: (Commission) -> Unit,
    onNavigateUserRequests: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
        HomeContent(
            state = state,
            onIntent,
            onNavigateProjects,
            onNavigateMessages,
            onNavigateUserRequests
        )
    }
}

@Composable
fun HomeContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    onNavigateProjects: () -> Unit,
    onNavigateMessages: (Commission) -> Unit,
    onNavigateUserRequests: () -> Unit
) {
    val currentUser = state.currentUser!!
    val commissions =
        state.commissions.filter { it.status != CommissionStatus.PENDING && it.status != CommissionStatus.REJECTED }
            .sortedByDescending { it.createdAt }.take(3)
    val notifications = state.notifications.sortedByDescending { it.createdAt }.take(3)
    val commissionRequests =
        state.commissions.filter { it.status == CommissionStatus.PENDING && it.freelancer.id == currentUser.id }

    val users = state.users
    val userRequests = state.users.filter { !it.isApproved && !it.isRejected }.sortedBy { it.createdAt }
    val activeUsers = state.users.filter { it.isOnline() }.sortedBy { it.createdAt }

    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        item {
            StatsCards(
                state = state,
                onIntent = onIntent
            )
        }

        if (currentUser.role == UserRole.CLIENT || currentUser.role == UserRole.FREELANCER)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFD3AD))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
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
                            modifier = Modifier
                                .padding(20.dp)
                                .padding(bottom = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Recent Projects",
                                    fontFamily = roboto,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F3F3F)
                                )
                                TextButton(
                                    onClick = onNavigateProjects
                                ) {
                                    Text(
                                        text = "View all >",
                                        fontFamily = roboto,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Thin,
                                        color = Color(0xFFFA812F)
                                    )
                                }
                            }
                            if (commissions.isNotEmpty()) {
                                commissions.take(3).forEach { commission ->
                                    CommissionCard(
                                        commission = commission,
                                        role = currentUser.role,
                                        onClick = { onNavigateMessages(commission) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        if (currentUser.role == UserRole.CLIENT)
            item {
                Spacer(Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFD3AD))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
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
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                text = "Recent Notifications",
                                fontFamily = roboto,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F3F3F)
                            )

                            if (notifications.isNotEmpty()) {
                                notifications.take(3).forEach { notification->
                                    NotificationItem(notification)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        if (currentUser.role == UserRole.FREELANCER)
            item {
                Spacer(Modifier.height(30.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFD3AD))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
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
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box {
                                    Text(
                                        text = "New Requests",
                                        fontFamily = roboto,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3F3F3F),
                                        modifier = Modifier.padding(top = 5.dp)
                                    )
                                    if (commissionRequests.count() > 0)
                                        Box(
                                            Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFA812F))
                                                .align(Alignment.TopEnd)
                                        )
                                }
                                TextButton(
                                    onClick = {
                                        onIntent(
                                            MainIntent.ShouldShowCommissionRequestsChanged(
                                                true
                                            )
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "View all >",
                                        fontFamily = roboto,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Thin,
                                        color = Color(0xFFFA812F)
                                    )
                                }
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "You have commission requests waiting for your approval.",
                                fontFamily = roboto,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFF3F3F3F)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        if (currentUser.role == UserRole.ADMIN) {
            if (userRequests.count() > 0)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFD3AD))
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
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
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box {
                                        Text(
                                            text = "${userRequests.count()} New Requests",
                                            fontFamily = roboto,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3F3F3F),
                                            modifier = Modifier.padding(top = 5.dp)
                                        )
                                        Box(
                                            Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFA812F))
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                    TextButton(
                                        onClick = onNavigateUserRequests
                                    ) {
                                        Text(
                                            text = "View all >",
                                            fontFamily = roboto,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Thin,
                                            color = Color(0xFFFA812F)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text = "You have user requests waiting for your approval.",
                                    fontFamily = roboto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    color = Color(0xFF3F3F3F)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFD3AD))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
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
                                text = "Active Users",
                                fontFamily = roboto,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F3F3F),
                                modifier = Modifier.padding(top = 5.dp)
                            )
                            Spacer(Modifier.height(15.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                                activeUsers.map { user ->
                                    ActiveUserCard(user)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun StatsCards(
    state: MainState,
    onIntent: (MainIntent) -> Unit
) {
    val firstName = state.currentUser!!.fullName.toTitleCase().trim().substringBefore(" ")
    val stats = state.stats!!
    val users = state.stats.users
    val role = state.currentUser.role

    Column(Modifier.padding(24.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .dropShadow(
                    shape = RoundedCornerShape(15.dp), shadow = Shadow(
                        radius = 4.dp,
                        color = Color.Black.copy(0.25f),
                        offset = DpOffset(x = 0.dp, y = (4).dp)
                    )
                )
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFFA812F))
                .padding(horizontal = 30.dp, vertical = 40.dp)
        ) {
            Text(
                "Welcome back, ",
                style = TextStyle(
                    fontFamily = AppFonts.roboto,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                ),
            )
            Spacer(Modifier.height(15.dp))
            Text(
                "$firstName!",
                style = TextStyle(
                    fontFamily = AppFonts.roboto,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                ),
            )
            Spacer(Modifier.height(30.dp))
            if (role == UserRole.ADMIN)
                Text(
                    "Hi Admin!",
                    style = TextStyle(
                        fontFamily = AppFonts.roboto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                )
            else
                Text(
                    "Manage your commission work",
                    style = TextStyle(
                        fontFamily = AppFonts.roboto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                )
        }
        Spacer(Modifier.height(20.dp))
        val statsText =
            if (role == UserRole.ADMIN) listOf("Active Users", "Freelancers", "Clients", "Requests")
            else listOf("Total Commissions", "In Progress", "Completed", "Total Points")
        val statsValue =
            if (role == UserRole.ADMIN) listOf(
                users.count { it.isOnline() },
                users.count { it.isOnline() && it.role == UserRole.FREELANCER },
                users.count { it.isOnline() && it.role == UserRole.CLIENT },
                stats.allUsersPoints.toAbbreviatedString()
            )
            else listOf(
                stats.totalCommissions,
                stats.inProgress,
                stats.completed,
                stats.totalPoints.toAbbreviatedString()
            )
        Row {
            Stat(
                Modifier.weight(4f),
                title = statsText[0],
                statDetail = {
                    Text(
                        "${statsValue[0]}",
                        style = TextStyle(
                            fontFamily = AppFonts.roboto,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF3F3F3F)
                        ),
                    )
                },
                body = "All Time"
            )
            Spacer(Modifier.width(30.dp))
            Stat(
                Modifier.weight(4f),
                title = statsText[1],
                statDetail = {
                    Text(
                        "${statsValue[1]}",
                        style = TextStyle(
                            fontFamily = AppFonts.roboto,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF009AE8)
                        ),
                    )
                },
                body = "Active Now"
            )
        }
        Spacer(Modifier.height(15.dp))
        Row {
            Stat(
                Modifier.weight(4f),
                title = statsText[2],
                statDetail = {
                    Text(
                        "${statsValue[2]}",
                        style = TextStyle(
                            fontFamily = AppFonts.roboto,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF70B562)
                        ),
                    )
                },
                body = if (role == UserRole.ADMIN) "Active Now" else "Finished"
            )
            Spacer(Modifier.width(30.dp))
            Stat(
                Modifier.weight(4f),
                title = statsText[3],
                statDetail = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.points_icon),
                            contentDescription = "Points",
                            tint = Color(0xFFAD6998),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "${statsValue[3]}",
                            style = TextStyle(
                                fontFamily = AppFonts.roboto,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFAD6998)
                            ),
                        )
                    }
                },
                body = "All Time"
            )
        }
    }
}

@Composable
fun Stat(
    modifier: Modifier, title: String,
    statDetail: @Composable () -> Unit,
    body: String
) {
    Column(
        modifier
            .height(180.dp)
            .dropShadow(
                shape = RoundedCornerShape(15.dp), shadow = Shadow(
                    radius = 4.dp,
                    color = Color.Black.copy(0.25f),
                    offset = DpOffset(x = 0.dp, y = (4).dp)
                )
            )
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFFEF3E2))
            .padding(horizontal = 30.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            title,
            style = TextStyle(
                fontFamily = AppFonts.roboto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3F3F3F)
            ),
        )
        Spacer(Modifier.height(5.dp))
        statDetail()
        Spacer(Modifier.height(5.dp))
        Text(
            body,
            style = TextStyle(
                fontFamily = AppFonts.roboto,
                fontSize = 15.sp,
                fontWeight = FontWeight.Thin,
                color = Color(0xFF3F3F3F)
            ),
        )
    }
}

val CardBackground = Color(0xFFFDF5E6)
val CardBorder = Color(0xFFEAE0D5)
val ChipBackground = Color(0xFFB39DDB)
val ChipContent = Color(0xFF4527A0)
val TextPrimary = Color(0xFF373737)
val TextSecondary = Color(0xFF757575)
val TextTertiary = Color(0xFFAAAAAA)
val AccentOrange = Color(0xFFFF8F5E)

@Composable
fun ProjectCard(
    projectName: String,
    clientName: String,
    dueDate: String,
    value: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = BorderStroke(1.5.dp, CardBorder),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    text = projectName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = clientName,
                    color = TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = "Due Date: $dueDate",
                    color = TextTertiary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }

            StatusChip(
                status = status,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.points_icon),
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value,
                    color = AccentOrange,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    var icon = R.drawable.info_icon
    var contentColor = Color.Unspecified
    var backgroundColor = Color.Unspecified

    when (status) {
        "In Progress" -> {
            icon = R.drawable.info_icon
            contentColor = Color(0xFF1700E6)
            backgroundColor = Color(0xFFC1BEDE)
        }

        "Pending" -> {
            icon = R.drawable.pending_icon
            contentColor = Color(0xFFBF9300)
            backgroundColor = Color(0xFFECDEAE)
        }

        "Done" -> {
            icon = R.drawable.info_icon
            contentColor = Color(0xFF389C1F)
            backgroundColor = Color(0xFFCAF5BF)
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(icon),
                contentDescription = status,
                tint = Color(0xFF1700E6),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = status,
                color = contentColor,
                fontSize = 12.sp,
                fontFamily = AppFonts.roboto,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun ActiveUserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3E2)),
        border = BorderStroke(1.dp, Color(0xFFDED4C5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.padding(top = 10.dp, end = 10.dp), contentAlignment = Alignment.TopEnd) {
            Row(
                Modifier
                    .background(Color(0xFFCAF5BF), shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.done_icon),
                    contentDescription = "Active User",
                    tint = Color(0xFF389C1F),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Active",
                    fontFamily = roboto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF389C1F),
                )
            }
            Column(
                Modifier.padding(vertical = 15.dp, horizontal = 12.dp),
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
            }
        }
    }
}

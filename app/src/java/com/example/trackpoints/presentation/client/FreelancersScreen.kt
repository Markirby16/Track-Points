package com.example.trackpoints.presentation.client

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.fallback
import coil3.request.placeholder
import com.example.trackpoints.R
import com.example.trackpoints.data.model.User
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.dummyFreelancers
import com.example.trackpoints.utils.isOnline
import com.example.trackpoints.utils.toTitleCase

@Composable
fun FreelancersScreen(
    state: MainState,
    onIntent: (MainIntent) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        FreelancersContent(
            state = state,
            onIntent = onIntent,
        )

        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
fun FreelancersContent(
    state: MainState,
    onIntent: (MainIntent) -> Unit,
    showRequestPage: Boolean = false
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }

    val currentUser = state.currentUser!!
    val freelancers = state.freelancers!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD3AD))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Find Freelancers",
            fontFamily = roboto,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F3F3F),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color(0xFFFEF3E2)
            ),
            placeholder = {
                Text(
                    "Search Freelancers..",
                    fontFamily = roboto,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = Color(0xFFFEF3E2)
                )
            },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.search_icon),
                    contentDescription = "Search",
                    tint = Color(0xFFFEF3E2),
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

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFDED4C5)),
            color = Color(0xFFFEF3E2)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xFFFEF3E2), shape = RoundedCornerShape(15.dp))
                    .border(1.dp, Color(0xFFDED4C5))
            ) {
                TabItem(
                    text = "All",
                    isSelected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        searchText = ""
                    },
                    modifier = Modifier.weight(1f)
                )
                TabItem(
                    text = "Recent",
                    isSelected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        searchText = ""
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filteredFreelancers = if (searchText.isNotBlank()) {
                freelancers.filter {
                    val search = searchText.lowercase()
                    val name = it.fullName.lowercase()
                    val specialty = it.specialty?.lowercase()
                    name.contains(search) || (specialty != null && specialty.contains(search))
                }
            } else {
                freelancers.filter {
                    val selection = if (selectedTab == 1) true else false
                    if (selection) it.isRecent else true
                }
            }

            items(filteredFreelancers, { freelancer -> freelancer.id }) { freelancer ->
                FreelancerCard(
                    freelancer,
                    { onIntent(MainIntent.CreateRequest(freelancer)) })
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFFA812F) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF3F3F3F),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun FreelancerCard(freelancer: User, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Color(0xFFDED4C5)),
        onClick = onClick,
        color = Color(0xFFFEF3E2)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (freelancer.photo == null)
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
                            .data(freelancer.photo)
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
                        contentDescription = "Chatmate Photo",
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.profile_icon_orange)
                    )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = freelancer.fullName.toTitleCase(),
                        fontFamily = roboto,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3F3F)
                    )
                    Text(
                        text = "Specialty (${freelancer.specialty ?: "N/A"})",
                        fontFamily = roboto,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF3F3F3F)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.star_icon),
                            null,
                            Modifier.size(10.dp),
                            tint = Color(0xFFE2AD00)
                        )
                        Text(
                            text = "${freelancer.rating}",
                            fontFamily = roboto,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3F3F3F)
                        )
                        Text(
                            text = "(${freelancer.totalProjects})",
                            fontFamily = roboto,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            color = Color(0xFF3F3F3F)
                        )
                    }
                }
                Spacer(Modifier.weight(0.2f))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (freelancer.isOnline()) Color(0xFF389C1F) else Color(0xFFA2A2A2),
                            CircleShape
                        )
                )
            }
            HorizontalDivider(color = Color(0xFFDED4C5), thickness = 1.dp)
            val projectsTogether = freelancer.projectsTogether ?: 0

            Text(
                text = "$projectsTogether Project${if (projectsTogether > 1) "s" else ""} Together",
                fontWeight = FontWeight.Light,
                color = Color(0xFF1700E6),
                fontFamily = roboto,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

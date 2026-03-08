package com.example.trackpoints.presentation.shared

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
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
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.InboxItem
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.MessageStatus
import com.example.trackpoints.data.model.MessageType
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.displayName
import com.example.trackpoints.utils.toRelativeTimeSpan
import com.example.trackpoints.utils.toTitleCase
import java.util.UUID

@Composable
fun MessagesScreen(
    state: MainState,
    onIntent: (MainIntent) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        MessagesContent(
            state = state,
            onIntent = onIntent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesContent(state: MainState, onIntent: (MainIntent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedCommission: Commission? by remember { mutableStateOf(null) }
    var message by remember { mutableStateOf("") }
    val hasFilledOut = selectedCommission != null && message.isNotBlank()

    val currentUser = state.currentUser!!
    val inbox = state.inbox

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Messages",
                        fontFamily = roboto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F3F3F)
                    )

                    Button(
                        onClick = { showBottomSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB1B1B1)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3F3F3F)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            painterResource(R.drawable.add_icon),
                            contentDescription = "Add",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF3F3F3F)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "New Message",
                            fontSize = 16.sp,
                            fontFamily = roboto,
                            color = Color(0xFF3F3F3F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(inbox, { inboxItem -> inboxItem.commission.id }) { inboxItem ->
                        MessageItem(
                            inboxItem,
                            currentUser.role,
                            { onIntent(MainIntent.StartChat(inboxItem.commission)) })
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color(0xFFFEF3E2),
            shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Message",
                        fontFamily = roboto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = { showBottomSheet = false }) {
                        Icon(
                            painterResource(R.drawable.close_icon),
                            contentDescription = "Close",
                            tint = Color(0xFF3F3F3F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "To",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    var firstName = ""
                    var commissionName = ""
                    if (selectedCommission != null) {
                        firstName = selectedCommission!!.freelancer.fullName
                            .substringBefore(" ")
                            .toTitleCase()
                        commissionName = selectedCommission!!.name
                    }
                    OutlinedTextField(
                        value = if (selectedCommission != null) "$firstName - (Project) $commissionName" else "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select Recipient...", color = Color(0xFF9CA3AF)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFD1D5DB).copy(alpha = 0.5f),
                            focusedContainerColor = Color(0xFFD1D5DB).copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFF97316)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        containerColor = Color(0xFFDADADA),
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        inbox.forEach { inboxItem ->
                            DropdownMenuItem(
                                text = {
                                    val firstName = inboxItem.chatmate.fullName
                                        .substringBefore(" ")
                                        .toTitleCase()
                                    val commissionName = inboxItem.commission.name

                                    Text("$firstName - (Project) $commissionName")
                                },
                                onClick = {
                                    selectedCommission = inboxItem.commission
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Message",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Type your message here..", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFD1D5DB).copy(alpha = 0.5f),
                        focusedContainerColor = Color(0xFFD1D5DB).copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFFF97316)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showBottomSheet = false
                        val chatMate =
                            if (currentUser.role == UserRole.CLIENT) selectedCommission!!.freelancer else selectedCommission!!.client

                        if (hasFilledOut) {
                            val newMessage = Message(
                                UUID.randomUUID().toString(),
                                selectedCommission!!.id,
                                currentUser.id,
                                chatMate.id,
                                message,
                                MessageType.TEXT,
                                status = MessageStatus.SENDING
                            )
                            onIntent(MainIntent.IsLoadingChanged(true))
                            onIntent(MainIntent.SendMessage(newMessage))
                            if (state.actionError.isBlank()) {
                                onIntent(MainIntent.StartChat(selectedCommission!!))
                                message = ""
                                selectedCommission = null
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFA812F)
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.send_icon),
                        contentDescription = "Send",
                        modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Message",
                        fontFamily = roboto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(inboxItem: InboxItem, role: UserRole, onClick: () -> Unit) {
    val user = inboxItem.chatmate
    val commission = inboxItem.commission
    val latestMessage = inboxItem.lastMessage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3E2)),
        border = BorderStroke(1.dp, Color(0xFFDED4C5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (user.photo == null)
                    Icon(
                        painterResource(R.drawable.profile_icon),
                        contentDescription = "Profile Photo",
                        tint = Color(0xFFFA812F),
                        modifier = Modifier.size(50.dp)
                    )
                else
                    AsyncImage(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
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
                        contentDescription = "Chatmate Photo",
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.profile_icon_orange)
                    )
                Spacer(Modifier.width(5.dp))
                Column(Modifier.fillMaxWidth(0.95f)) {
                    Text(
                        commission.name.toTitleCase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        user.fullName.toTitleCase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    }
                    Spacer(Modifier.height(3.dp))
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
                    Spacer(Modifier.height(3.dp))
                    Text(
                        latestMessage.content ?: "Last message displays here",
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        latestMessage?.createdAt?.toRelativeTimeSpan() ?: "",
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (inboxItem.unreadCount > 0)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color = Color(0xFFFA812F), shape = CircleShape)
                    )
            }
        }
    }
}

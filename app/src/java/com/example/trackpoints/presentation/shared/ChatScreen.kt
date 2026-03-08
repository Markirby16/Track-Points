package com.example.trackpoints.presentation.shared

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.trackpoints.core.ui.ImageViewer
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.MessageStatus
import com.example.trackpoints.data.model.MessageType
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.presentation.main.MainEffect
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.presentation.main.MainViewModel
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.utils.toTitleCase
import io.github.jan.supabase.realtime.Column
import java.util.UUID

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        ChatContent(state = state, onIntent = viewModel::handleIntent)
        if (state.actionError.isNotBlank()) {
            Alert(
                title = "Error",
                message = state.actionError,
                onDismissRequest = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
                confirmButtonText = "Ok",
                onConfirmClicked = { viewModel.handleIntent(MainIntent.ActionErrorChanged("")) },
            )
        }
        if (state.isLoading)
            LoadingOverlay()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChatContent(state: MainState, onIntent: (MainIntent) -> Unit) {
    val currentUser = state.currentUser!!
    val commission = state.currentChatmate!!
    val chatMate =
        if (currentUser.role == UserRole.CLIENT) commission.freelancer else commission.client
    val messages = state.currentChatHistory

    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var selectedImage by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_icon),
                        contentDescription = "Back",
                        tint = Color(0xFF3F3F3F),
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 20.dp, start = 10.dp)
                            .size(24.dp)
                            .clickable { onIntent(MainIntent.BackClicked) }
                    )
                },
                title = {
                    Row(
                        Modifier
                            .padding(top = 20.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chatMate.photo == null)
                            Icon(
                                painter = painterResource(R.drawable.profile_icon),
                                contentDescription = "Chatmate",
                                modifier = Modifier.size(35.dp)
                            )
                        else
                            AsyncImage(
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape),
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(chatMate.photo)
                                    .fallback(R.drawable.profile_icon)
                                    .placeholder(R.drawable.profile_icon)
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
                                error = painterResource(R.drawable.profile_icon)
                            )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            chatMate.fullName.toTitleCase(),
                            style = TextStyle(
                                fontFamily = AppFonts.roboto,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF3E2)
                ),
                modifier = Modifier
                    .dropShadow(
                        shape = RoundedCornerShape(0.dp), shadow = Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(0.25f),
                            offset = DpOffset(x = 0.dp, y = (4).dp)
                        )
                    )
            )
        },
    ) { paddingValues ->
        Box {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .background(Color(0xFFFFD3AD))
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
                        ),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    item {
                        Spacer(Modifier.height(5.dp))
                    }
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message, currentUserId = currentUser.id,
                            isSelected = selectedMessageId == message.id,
                            onClick = {
                                selectedMessageId =
                                    if (selectedMessageId == message.id) null else message.id
                            },
                            onClickImage = {
                                selectedImage = it
                            }
                        )
                    }
                    item {
                        Spacer(Modifier.height(5.dp))
                    }
                }
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .dropShadow(
                                shape = RoundedCornerShape(0.dp), shadow = Shadow(
                                    radius = 4.dp,
                                    color = Color.Black.copy(0.25f),
                                    offset = DpOffset(x = 0.dp, y = (-4).dp)
                                )
                            )
                            .background(Color(0xFFFEF3E2))
                            .padding(horizontal = 10.dp)
                            .padding(top = 10.dp)
                            .padding(bottom = paddingValues.calculateBottomPadding()),
                    ) {
                        ChatInputBar(
                            text = textState,
                            onTextChange = { textState = it },
                            onSendClick = {
                                if (textState.isNotBlank()) {
                                    textState = textState.trim()
                                    val newMessage = Message(
                                        UUID.randomUUID().toString(),
                                        commission.id,
                                        currentUser.id,
                                        chatMate.id,
                                        textState,
                                        MessageType.TEXT,
                                        status = MessageStatus.SENDING
                                    )
                                    onIntent(MainIntent.SendMessage(newMessage))
                                    textState = ""
                                }
                            },
                            commission.id,
                            currentUser.id,
                            chatMate.id,
                            onIntent
                        )
                    }
                }
            }
            if (selectedImage.isNotBlank())
                ImageViewer(imageUrl = selectedImage, onDismiss = { selectedImage = "" })
        }
    }
}

@Composable
fun ChatBubble(
    message: Message,
    currentUserId: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onClickImage: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val isSentByMe = currentUserId == message.senderId

    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isSentByMe) Color.White else Color(0xFFFA812F)
    val textColor = if (isSentByMe) Color.Black else Color.White

    val bubbleShape = if (isSentByMe) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 24.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
        ) {
            if (message.messageType == MessageType.TEXT)
                Surface(
                    color = backgroundColor,
                    shape = bubbleShape,
                    modifier = Modifier
                        .widthIn(max = screenWidth * 0.75f)
                        .clip(bubbleShape)
                        .clickable { onClick() }
                ) {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            if (message.messageType == MessageType.IMAGE)
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth(0.49f)
                        .aspectRatio(1f)
                        .padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onClickImage(message.content) },
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.content)
                        .placeholder(R.drawable.loading_photo)
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
                    contentDescription = "Image Message",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.image_icon)
                )
            if (isSentByMe) {
                if (message.status == MessageStatus.SENDING)
                    AnimatedVisibility(visible = true) {
                        Text(
                            text = message.status.toString().toTitleCase(),
                            color = Color(0xFFB1B1B1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                        )
                    }
                else
                    AnimatedVisibility(visible = isSelected) {
                        Text(
                            text = if (message.readAt) "Seen" else "Delivered",
                            color = Color(0xFFB1B1B1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                        )
                    }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: (String) -> Unit,
    commissionId: String,
    senderId: String,
    receiverId: String,
    onIntent: (MainIntent) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
//            .imePadding()
//            .padding(vertical = 10.dp)
            .background(Color(0xFFFEF3E2))
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                if (uri != null) {
                    val newMessage = Message(
                        UUID.randomUUID().toString(),
                        commissionId,
                        senderId,
                        receiverId,
                        uri.toString(),
                        MessageType.IMAGE,
                        status = MessageStatus.SENDING
                    )
                    onIntent(MainIntent.SendImageMessage(newMessage, uri, context))
                }
            }
        )
        Icon(
            painterResource(R.drawable.image_icon),
            contentDescription = "Send Image",
            tint = Color(0xFFFA812F),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    "Type a message...",
                    fontFamily = AppFonts.roboto
                )
            },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp)
                .heightIn(min = 56.dp),
            maxLines = 3,
            singleLine = false,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.Black,
                focusedBorderColor = Color.Black,
            )
        )
        IconButton(
            onClick = { onSendClick(text) },
            enabled = text.isNotBlank(),
            modifier = Modifier.background(Color(0xFFFA812F), CircleShape),
        ) {
            Icon(
                painter = painterResource(R.drawable.send_icon),
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
package com.example.trackpoints.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.trackpoints.R
import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.presentation.main.MainState
import com.example.trackpoints.ui.theme.AppFonts.roboto
import com.example.trackpoints.utils.toRelativeTimeSpan

@Composable
fun NotificationsDialog(
    state: MainState,
    onDismissRequest: () -> Unit = {}
) {
    val notifications = state.notifications.sortedByDescending { it.createdAt }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
            shape = RoundedCornerShape(15.dp),
            color = Color(0xFFFEF3E2),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
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
                        text = "Notifications",
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
                    items(notifications, { notification -> notification.id }) { notification ->
                        NotificationItem(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        var color = Color(0xFFFA812F)
//        if (notification.message.contains("Bla bla")) color = Color(0xFF1700E6)
//        else color = Color(0xFF389C1F)

        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(Modifier.clickable {}) {
            Text(
                text = notification.message,
                fontSize = 14.sp,
                fontFamily = roboto,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF3F3F3F)
            )
            Text(
                text = notification.createdAt.toRelativeTimeSpan(),
                fontSize = 15.sp,
                fontFamily = roboto,
                fontWeight = FontWeight.Thin,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF3F3F3F)
            )
        }
    }
}

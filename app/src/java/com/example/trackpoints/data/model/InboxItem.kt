package com.example.trackpoints.data.model

import com.example.trackpoints.utils.JavaInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class InboxItem(
    val commission: Commission,
    val chatmate: User,
    @SerialName("last_message")
    val lastMessage: Message,
    @SerialName("is_from_me")
    val isFromMe: Boolean,
    @SerialName("unread_count")
    val unreadCount: Long
)

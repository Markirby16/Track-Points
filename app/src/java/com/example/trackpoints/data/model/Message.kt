package com.example.trackpoints.data.model

import com.example.trackpoints.utils.JavaInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Message(
    val id: String = "",

    @SerialName("commission_id")
    val commissionId: String? = null,

    @SerialName("sender_id")
    val senderId: String? = null,

    @SerialName("receiver_id")
    val receiverId: String? = null,

    val content: String,

    @SerialName("message_type")
    val messageType: MessageType,

    @SerialName("attachment_url")
    val attachmentUrl: String? = null,

    @SerialName("attachment_name")
    val attachmentName: String? = null,

    @SerialName("attachment_mime")
    val attachmentMime: String? = null,

    @SerialName("read_at")
    val readAt: Boolean = true,

    @SerialName("created_at")
    @Serializable(with = JavaInstantSerializer::class)
    val createdAt: Instant = Instant.now(),

    val status: MessageStatus = MessageStatus.DELIVERED
)

enum class MessageStatus {
    SENDING,
    DELIVERED
}

enum class MessageType {
    DOCUMENT,
    IMAGE,
    TEXT,
}

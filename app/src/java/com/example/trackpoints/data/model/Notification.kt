package com.example.trackpoints.data.model

import com.example.trackpoints.utils.JavaInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Notification(
    val id: String,

    val message: String,

    @SerialName("is_read")
    val isRead: Boolean,

    @SerialName("created_at")
    @Serializable(with = JavaInstantSerializer::class)
    val createdAt: Instant,
)

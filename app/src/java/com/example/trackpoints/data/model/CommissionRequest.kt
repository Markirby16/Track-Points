package com.example.trackpoints.data.model

import com.example.trackpoints.utils.JavaInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class CommissionRequest(
    val name: String,

    @SerialName("client_id")
    val clientId: String,

    @SerialName("freelancer_id")
    val freelancerId: String,

    val status: CommissionStatus = CommissionStatus.PENDING,

    val points: Int,

    val description: String,

    @SerialName("due_date")
    @Serializable(with = JavaInstantSerializer::class)
    val dueDate: Instant,

    @SerialName("created_at")
    @Serializable(with = JavaInstantSerializer::class)
    val createdAt: Instant = Instant.now(),
)

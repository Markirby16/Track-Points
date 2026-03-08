package com.example.trackpoints.data.model

import com.example.trackpoints.utils.JavaInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Commission(
    val id: String,

    val name: String,

    val client: User,

    val freelancer: User,

    val status: CommissionStatus,

    val points: Int,

    val rating: Float? = null,

    val description: String,

    @SerialName("due_date")
    @Serializable(with = JavaInstantSerializer::class)
    val dueDate: Instant,

    @SerialName("date_paid")
    @Serializable(with = JavaInstantSerializer::class)
    val datePaid: Instant? = null,

    @SerialName("created_at")
    @Serializable(with = JavaInstantSerializer::class)
    val createdAt: Instant,
)

enum class CommissionStatus {
    REJECTED,
    IN_PROGRESS,
    PENDING,
    DONE
}

package com.example.trackpoints.data.model

import com.example.trackpoints.utils.JavaInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class User(
    val id: String,

    val email: String,

    val role: UserRole,

    @SerialName("fullname")
    val fullName: String,

    @SerialName("is_approved")
    val isApproved: Boolean = false,

    @SerialName("is_rejected")
    val isRejected: Boolean = false,

    @SerialName("created_at")
    @Serializable(with = JavaInstantSerializer::class)
    val createdAt: Instant,

    @SerialName("phone_number")
    val phoneNumber: String? = "",

    @SerialName("last_seen")
    @Serializable(with = JavaInstantSerializer::class)
    val lastSeen: Instant? = null,

    val specialty: String? = "",

    val photo: String? = null,

    val portfolio: List<String>? = null,

    val rating: Float? = 5.0f,

    @SerialName("total_projects")
    val totalProjects: Int? = 0,

    @SerialName("projects_together")
    val projectsTogether: Int? = 0,

    @SerialName("is_recent")
    val isRecent: Boolean = false
)

enum class UserRole {
    FREELANCER,
    CLIENT,
    ADMIN
}

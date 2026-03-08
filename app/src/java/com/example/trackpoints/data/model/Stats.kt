package com.example.trackpoints.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    @SerialName("total_commissions")
    val totalCommissions: Int = 0,

    @SerialName("in_progress")
    val inProgress: Int = 0,

    val completed: Int = 0,

    @SerialName("total_points")
    val totalPoints: Int = 0,

    @SerialName("all_users_points")
    val allUsersPoints: Int = 0,

    val users: List<User> = emptyList()
)

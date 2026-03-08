package com.example.trackpoints.utils

import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.User
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toTitleCase(
    exclusions: List<String> = listOf(
        "of",
        "the",
        "and",
        "in",
        "on",
        "at"
    )
): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            if (word in exclusions) {
                word
            } else {
                word.replaceFirstChar { it.uppercase() }
            }
        }
}

fun Int.toAbbreviatedString(): String {
    return when {
        this >= 1_000_000 -> "${String.format("%.2f", this / 1_000_000.0).removeSuffix(".00")}M"
        this >= 1_000 -> "${String.format("%.2f", this / 1_000.0).removeSuffix(".00")}K"
        else -> this.toString()
    }
}

fun Instant?.toDashDateString(): String {
    if (this == null) return "N/A"

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())

    return formatter.format(this)
}

fun Instant.toRelativeTimeSpan(): String {
    val now = Instant.now()
    val duration = Duration.between(this, now)
    val seconds = duration.seconds

    return when {
        seconds < 10 -> "Just now"
        seconds < 60 -> "$seconds seconds ago"
        seconds < 3600 -> {
            val mins = seconds / 60
            if (mins == 1L) "1 minute ago" else "$mins minutes ago"
        }

        seconds < 86400 -> {
            val hours = seconds / 3600
            if (hours == 1L) "1 hour ago" else "$hours hours ago"
        }

        seconds < 604800 -> {
            val days = seconds / 86400
            if (days == 1L) "1 day ago" else "$days days ago"
        }

        else -> this.toDashDateString()
    }
}

val CommissionStatus.displayName: String
    get() = when (this) {
        CommissionStatus.IN_PROGRESS -> "In Progress"
        CommissionStatus.PENDING -> "Pending"
        CommissionStatus.REJECTED -> "Rejected"
        CommissionStatus.DONE -> "Done"
    }

fun User.isOnline(): Boolean {
    val lastActive = this.lastSeen ?: return false

    val now = Instant.now()
    val duration = Duration.between(lastActive, now)

    return duration.abs().toMinutes() < 5
}
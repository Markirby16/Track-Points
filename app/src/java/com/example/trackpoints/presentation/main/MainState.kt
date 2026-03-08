package com.example.trackpoints.presentation.main

import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.InboxItem
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.data.model.Stats
import com.example.trackpoints.data.model.User

data class MainState(
    val currentUser: User? = null,
    val actionError: String = "",
    val isLoading: Boolean = false,
    val shouldLogout: Boolean = false,
    val shouldShowCommissionRequests: Boolean = false,
    val shouldShowUserRequests: Boolean = false,
    val shouldShowNotifications: Boolean = false,
    val dataFetchStatus: StateFetchStatus = StateFetchStatus(),
    val stats: Stats? = null,
    val notifications: List<Notification> = emptyList(),
    val users: List<User> = emptyList(),
    val freelancers: List<User> = emptyList(),
    val commissions: List<Commission> = emptyList(),
    val inbox: List<InboxItem> = emptyList(),
    val currentChatmate: Commission? = null,
    val currentChatHistory: List<Message> = emptyList(),
    val currentFreelancerToRequest: User? = null,
)

data class StateFetchStatus(
    val currentUser: FetchStatus = FetchStatus.LOADING,
    val stats: FetchStatus = FetchStatus.LOADING,
    val notifications: FetchStatus = FetchStatus.LOADING,
    val users: FetchStatus = FetchStatus.LOADING,
    val freelancers: FetchStatus = FetchStatus.LOADING,
    val commissions: FetchStatus = FetchStatus.LOADING,
    val inbox: FetchStatus = FetchStatus.LOADING,
    val chatHistory: FetchStatus = FetchStatus.LOADING,
)

enum class FetchStatus {
    LOADING,
    DONE,
}

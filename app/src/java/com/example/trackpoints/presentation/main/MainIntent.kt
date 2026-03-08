package com.example.trackpoints.presentation.main

import android.content.Context
import android.net.Uri
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionRequest
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.User

sealed interface MainIntent {
    data class ActionErrorChanged(val value: String) : MainIntent
    data class IsLoadingChanged(val value: Boolean) : MainIntent
    data class ShouldLogoutChanged(val value: Boolean) : MainIntent
    data class ShouldShowCommissionRequestsChanged(val value: Boolean) : MainIntent
    data class ShouldShowUserRequestsChanged(val value: Boolean) : MainIntent
    data class ShouldShowNotificationsChanged(val value: Boolean) : MainIntent
    data class StartChat(val value: Commission) : MainIntent
    data class SendMessage(val value: Message) : MainIntent
    data class SendImageMessage(val value: Message, val uri: Uri, val context: Context) : MainIntent
    data class UpdateProfilePic(val uri: Uri, val context: Context) : MainIntent
    data class UpdatePortfolioPics(val uris: List<Uri>, val context: Context) : MainIntent
    data class UpdateProfileDetail(val phoneNumber: String, val specialty: String = "") : MainIntent
    data class MarkCommissionRequest(val shouldAccept: Boolean, val commissionId: String) :
        MainIntent

    data class MarkUserRequest(val shouldAccept: Boolean, val userId: String) : MainIntent
    data class SendPointsAndRating(val value: Commission, val rating: Int) : MainIntent
    data class SubmitRequest(val value: CommissionRequest) : MainIntent
    data class CreateRequest(val value: User) : MainIntent
    data class MarkMessagesAsRead(val value: String) : MainIntent
    data object StopChat : MainIntent
    data object MarkCurrentNotificationsAsRead : MainIntent
    data object FetchStats : MainIntent
    data object FetchNotifications : MainIntent
    data object FetchUsers : MainIntent
    data object FetchFreelancers : MainIntent
    data object FetchCommissions : MainIntent
    data object FetchInbox : MainIntent
    data object FetchChatHistory : MainIntent
    data object FetchAndObserveChats : MainIntent
    data object ProfileClicked : MainIntent
    data object BackClicked : MainIntent
    data object LogoutClicked : MainIntent
}

sealed interface MainEffect {
    data object NavigateRequest : MainEffect
    data object NavigateChat : MainEffect
    data object NavigateProfile : MainEffect
    data object NavigateBack : MainEffect
    data object NavigateLogout : MainEffect
}
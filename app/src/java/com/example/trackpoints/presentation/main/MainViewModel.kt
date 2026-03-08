package com.example.trackpoints.presentation.main

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.CommissionRequest
import com.example.trackpoints.data.model.InboxItem
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.MessageStatus
import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.data.model.Stats
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.model.UserRole
import com.example.trackpoints.data.remote.ApiResult
import com.example.trackpoints.data.repository.AuthRepository
import com.example.trackpoints.data.repository.CommissionRepository
import com.example.trackpoints.data.repository.MessageRepository
import com.example.trackpoints.data.repository.NotificationRepository
import com.example.trackpoints.data.repository.UserRepository
import com.example.trackpoints.utils.Utils
import com.example.trackpoints.utils.toTitleCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MainViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val commissionRepository: CommissionRepository,
    private val messageRepository: MessageRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MainEffect>()
    val effect: SharedFlow<MainEffect> = _effect.asSharedFlow()

    private var pollingJob: Job? = null
    private var chatObservationJob: Job? = null

    fun startPolling() {
        pollingJob?.cancel()
        val currentUser = _state.value.currentUser!!

        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    Log.d("POLL", "CURRENTLY POLLING")
                    if (!authRepository.isLoggedIn()) {
                        Log.d("POLL", "STOPPED POLLING")
                        pollingJob?.cancel()
                    }

                    if (currentUser.role == UserRole.ADMIN) {
                        fetchUsers()
                    } else {
                        fetchInbox()
                        fetchFreelancers()
                        fetchCommissions()
                    }
                    fetchStats()
                    updateLastSeen()
                    fetchNotifications()
                } catch (e: Exception) {
                }
                delay(5_000L)
            }
        }
    }

    fun checkDeadlines() {
        viewModelScope.launch {
            commissionRepository.checkDeadlines()
        }
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.ActionErrorChanged -> _state.update { it.copy(actionError = intent.value) }
            is MainIntent.IsLoadingChanged -> _state.update { it.copy(isLoading = intent.value) }
            is MainIntent.ShouldLogoutChanged -> _state.update { it.copy(shouldLogout = intent.value) }
            is MainIntent.ShouldShowNotificationsChanged -> {
                _state.update { it.copy(shouldShowNotifications = intent.value) }
                markCurrentNotificationsAsRead()
            }

            is MainIntent.ShouldShowCommissionRequestsChanged -> _state.update {
                it.copy(
                    shouldShowCommissionRequests = intent.value
                )
            }

            is MainIntent.ShouldShowUserRequestsChanged -> _state.update {
                it.copy(
                    shouldShowUserRequests = intent.value
                )
            }

            is MainIntent.SendMessage -> sendMessage(intent.value)
            is MainIntent.SendImageMessage -> sendImageMessage(
                intent.value,
                intent.uri,
                intent.context
            )

            is MainIntent.UpdateProfilePic -> updateProfilePic(
                intent.uri,
                intent.context
            )

            is MainIntent.UpdatePortfolioPics -> updatePortfolioPics(
                intent.uris,
                intent.context
            )

            is MainIntent.UpdateProfileDetail -> updateProfileDetail(
                intent.phoneNumber,
                intent.specialty
            )

            is MainIntent.StartChat -> startChat(intent.value)
            is MainIntent.StopChat -> stopChat()
            is MainIntent.MarkMessagesAsRead -> markMessagesAsRead(intent.value)
            is MainIntent.MarkCurrentNotificationsAsRead -> markCurrentNotificationsAsRead()
            is MainIntent.MarkCommissionRequest -> markCommissionRequest(
                intent.shouldAccept, intent.commissionId
            )

            is MainIntent.MarkUserRequest -> markUserRequest(intent.shouldAccept, intent.userId)
            is MainIntent.SubmitRequest -> submitRequest(intent.value)

            is MainIntent.CreateRequest -> {
                _state.update { it.copy(currentFreelancerToRequest = intent.value) }
                sendEffect(MainEffect.NavigateRequest)
            }

            is MainIntent.SendPointsAndRating -> sendPointsAndRating(intent.value, intent.rating)

            is MainIntent.FetchStats -> fetchStats()
            is MainIntent.FetchCommissions -> fetchCommissions()
            is MainIntent.FetchInbox -> fetchInbox()
            is MainIntent.FetchChatHistory -> fetchChatHistory()
            is MainIntent.FetchAndObserveChats -> fetchAndObserveChats()
            is MainIntent.FetchUsers -> fetchUsers()
            is MainIntent.FetchFreelancers -> fetchFreelancers()
            is MainIntent.FetchNotifications -> fetchNotifications()
            is MainIntent.ProfileClicked -> sendEffect(MainEffect.NavigateProfile)
            is MainIntent.BackClicked -> sendEffect(MainEffect.NavigateBack)
            is MainIntent.LogoutClicked -> logout()
        }
    }

    private fun stopChat() {
        Log.d("STOP_CHAT", "STOPPING CHAT")
        chatObservationJob?.cancel()
        chatObservationJob = null
    }

    private fun markMessagesAsRead(commissionId: String) {
        viewModelScope.launch {
            val markMessageAsReadResult = messageRepository.markMessagesAsRead(commissionId)
            var actionError = ""

            when (markMessageAsReadResult) {
                is ApiResult.Success -> {
                    Log.d("MARK_MESSAGES", "MARKED MESSAGES AS READ")
                }

                is ApiResult.Error -> {
                    actionError = markMessageAsReadResult.message
                    Log.d("MARK_MESSAGES", actionError)
                }
            }
        }
    }

    private fun fetchAndObserveChats() {
        chatObservationJob?.cancel()
        chatObservationJob = viewModelScope.launch {
            val currentUser = _state.value.currentUser!!
            val commission = _state.value.currentChatmate!!
            val chatmate =
                if (currentUser.role == UserRole.CLIENT) commission.freelancer else commission.client

            val realtimeMessagesResult = messageRepository.getRealtimeMessages(chatmate.id)

            var history = _state.value.currentChatHistory
            var actionError = ""

            when (realtimeMessagesResult) {
                is ApiResult.Success -> {
                    realtimeMessagesResult.data.collect { newMessage ->
                        history =
                            (history + newMessage).distinctBy { it.id }
                                .sortedBy { it.createdAt }

                        markMessagesAsRead(commission.id)
                        _state.update { it.copy(currentChatHistory = history) }
                    }
                }

                is ApiResult.Error -> {
                    actionError = realtimeMessagesResult.message
                    Log.d("CHAT", actionError)
                }
            }
        }
    }

    private fun updateProfileDetail(phoneNumber: String, specialty: String) {
        val isValid = phoneNumber.matches(Regex("^9[0-9]{9}$"))
        if (!isValid) {
            handleIntent(MainIntent.IsLoadingChanged(false))
            handleIntent(MainIntent.ActionErrorChanged("Invalid phone number, please try again"))
            return
        }

        viewModelScope.launch {
            val id = _state.value.currentUser!!.id
            val updates = buildJsonObject {
                put("phone_number", phoneNumber)
                put("specialty", specialty)
            }

            val updateProfileDetailResult = userRepository.updateUserProfile(id, updates)
            var actionError = ""

            when (updateProfileDetailResult) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> actionError = updateProfileDetailResult.message
            }

            _state.update { it.copy(actionError = actionError) }

            if (actionError.isBlank()) {
                val userResult = userRepository.getCurrentUserProfile()

                when (userResult) {
                    is ApiResult.Success -> {
                        _state.update { it.copy(currentUser = userResult.data) }
                        handleIntent(MainIntent.ActionErrorChanged("Successfully updated profile details!"))
                    }

                    is ApiResult.Error -> Log.d("MAIN", userResult.message)
                }
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun updatePortfolioPics(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            val id = _state.value.currentUser!!.id
            val portfolioPicsResult = userRepository.updatePortfolioPics(id, uris, context)
            var actionError = ""

            when (portfolioPicsResult) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> actionError = portfolioPicsResult.message
            }

            _state.update { it.copy(actionError = actionError) }

            if (actionError.isBlank()) {
                val userResult = userRepository.getCurrentUserProfile()

                when (userResult) {
                    is ApiResult.Success -> {
                        _state.update { it.copy(currentUser = userResult.data) }
                        handleIntent(MainIntent.ActionErrorChanged("Successfully updated portfolio!"))
                    }

                    is ApiResult.Error -> Log.d("MAIN", userResult.message)
                }
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun updateProfilePic(uri: Uri, context: Context) {
        viewModelScope.launch {
            val id = _state.value.currentUser!!.id
            val profilePicResult = userRepository.updateProfilePic(id, uri, context)
            var actionError = ""

            when (profilePicResult) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> actionError = profilePicResult.message
            }

            _state.update { it.copy(actionError = actionError) }

            if (actionError.isBlank()) {
                val userResult = userRepository.getCurrentUserProfile()

                when (userResult) {
                    is ApiResult.Success -> {
                        _state.update { it.copy(currentUser = userResult.data) }
                        handleIntent(MainIntent.ActionErrorChanged("Successfully updated profile picture!"))
                    }

                    is ApiResult.Error -> Log.d("MAIN", userResult.message)
                }
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun sendImageMessage(message: Message, uri: Uri, context: Context) {
        viewModelScope.launch {
            _state.update { it ->
                it.copy(
                    currentChatHistory = it.currentChatHistory + message,
                )
            }

            val commissionId = _state.value.currentChatmate!!.id
            val sendImageMessageResult =
                messageRepository.uploadImage(commissionId, uri, context)
            var actionError = ""

            when (sendImageMessageResult) {
                is ApiResult.Success -> {
                    val sendMessageResults =
                        messageRepository.sendMessage(
                            message.copy(
                                content = sendImageMessageResult.data,
                                status = MessageStatus.DELIVERED
                            )
                        )
                    var actionError = ""
                    when (sendMessageResults) {
                        is ApiResult.Success -> {
                            _state.update { currentState ->
                                val updatedHistory =
                                    currentState.currentChatHistory.map { lastMessage ->
                                        if (lastMessage.id == message.id) lastMessage.copy(
                                            status = MessageStatus.DELIVERED
                                        ) else lastMessage
                                    }
                                currentState.copy(currentChatHistory = updatedHistory)
                            }
                        }

                        is ApiResult.Error -> {
                            actionError = sendMessageResults.message
                            Log.d("SEND", actionError)
                        }
                    }
                }

                is ApiResult.Error -> {
                    actionError = sendImageMessageResult.message
                    Log.d("SEND", actionError)
                }
            }
        }
    }

    private fun fetchChatHistory() {
        viewModelScope.launch {
            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(chatHistory = FetchStatus.LOADING)) }
            handleIntent(MainIntent.IsLoadingChanged(true))
            val commissionId = _state.value.currentChatmate!!.id
            val fetchChatHistoryResult = messageRepository.getChatHistory(commissionId)
            var chatHistory: List<Message> = emptyList()
            var actionError = ""

            when (fetchChatHistoryResult) {
                is ApiResult.Success -> {
                    chatHistory = fetchChatHistoryResult.data
                    Log.d("CHAT_HISTORY", chatHistory.toString())
                }

                is ApiResult.Error -> {
                    actionError = fetchChatHistoryResult.message
                    Log.d("CHAT_HISTORY", actionError)
                }
            }

            _state.update {
                it.copy(
                    currentChatHistory = chatHistory,
                    dataFetchStatus = it.dataFetchStatus.copy(chatHistory = FetchStatus.DONE)
                )
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun fetchInbox() {
        viewModelScope.launch {
            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(inbox = FetchStatus.LOADING)) }
            val fetchInboxResult = messageRepository.getInbox()
            var inbox: List<InboxItem> = emptyList()
            var actionError = ""

            when (fetchInboxResult) {
                is ApiResult.Success -> {
                    inbox = fetchInboxResult.data
                }

                is ApiResult.Error -> {
                    actionError = fetchInboxResult.message
                    Log.d("INBOX", actionError)
                }
            }

            _state.update {
                it.copy(
                    inbox = inbox,
                    dataFetchStatus = it.dataFetchStatus.copy(inbox = FetchStatus.DONE)
                )
            }
        }
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(users = FetchStatus.LOADING)) }
            val fetchUsersResult = userRepository.getUsers()
            var users: List<User> = emptyList()
            var actionError = ""

            when (fetchUsersResult) {
                is ApiResult.Success -> users = fetchUsersResult.data
                is ApiResult.Error -> actionError = fetchUsersResult.message
            }

            _state.update {
                it.copy(
                    users = users,
                    dataFetchStatus = it.dataFetchStatus.copy(users = FetchStatus.DONE)
                )
            }
        }
    }

    private fun submitRequest(newCommission: CommissionRequest) {
        val actionError = Utils.validateCommission(newCommission)
        if (actionError != null) {
            handleIntent(MainIntent.IsLoadingChanged(false))
            handleIntent(MainIntent.ActionErrorChanged(actionError))
            return
        }

        viewModelScope.launch {
            val submitRequestResult = commissionRepository.createCommission(newCommission)
            var actionError = ""

            when (submitRequestResult) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> actionError = submitRequestResult.message
            }

            _state.update { it.copy(actionError = actionError) }

            if (actionError.isBlank()) {
                val id = _state.value.currentUser!!.id
                val commissionResult = commissionRepository.getCommissions(id)

                when (commissionResult) {
                    is ApiResult.Success -> {
                        _state.update { it.copy(commissions = commissionResult.data) }
                        handleIntent(MainIntent.ActionErrorChanged("Successfully submitted commission request!"))
                    }

                    is ApiResult.Error -> actionError = commissionResult.message
                }

                if (actionError.isNotBlank())
                    _state.update { it.copy(actionError = actionError) }
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun sendPointsAndRating(commission: Commission, rating: Int) {
        viewModelScope.launch {
            val currentUser = _state.value.currentUser!!
            val sendPointsAndRatingResult =
                commissionRepository.updateCommission(commission.id, rating)

            val actionError = when (sendPointsAndRatingResult) {
                is ApiResult.Success -> {
                    fetchCommissions()
                    fetchStats()
                    if (currentUser.role == UserRole.CLIENT) fetchFreelancers()
                    "Successfully sent points to ${commission.freelancer.fullName.toTitleCase()}"
                }

                is ApiResult.Error -> sendPointsAndRatingResult.message
            }

            _state.update {
                it.copy(actionError = actionError)
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun markUserRequest(shouldAccept: Boolean, userId: String) {
        viewModelScope.launch {
            val markUserRequestResult =
                userRepository.approveOrRejectUserRequest(shouldAccept, userId)

            val actionError = when (markUserRequestResult) {
                is ApiResult.Success -> "Successfully ${if (shouldAccept) "approved" else "rejected"} user"
                is ApiResult.Error -> markUserRequestResult.message
            }

            _state.update {
                it.copy(actionError = actionError)
            }
            handleIntent(MainIntent.FetchUsers)
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun markCommissionRequest(shouldAccept: Boolean, commissionId: String) {
        viewModelScope.launch {
            val markCommissionResult =
                commissionRepository.markCommissionRequest(commissionId, shouldAccept)

            val actionError = when (markCommissionResult) {
                is ApiResult.Success -> "Successfully ${if (shouldAccept) "approved" else "rejected"} commission request"
                is ApiResult.Error -> markCommissionResult.message
            }

            _state.update {
                it.copy(actionError = actionError)
            }
            handleIntent(MainIntent.FetchCommissions)
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    private fun markCurrentNotificationsAsRead() {
        val notifications = _state.value.notifications
        val latestNotification = notifications.maxByOrNull { it.createdAt }

        latestNotification?.let { latest ->
            viewModelScope.launch {
                notificationRepository.markCurrentNotificationsAsRead(latest)
            }
        }
    }

    private fun startChat(chatMate: Commission) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    currentChatmate = chatMate,
                    currentChatHistory = emptyList()
                )
            }
            delay(300)
            sendEffect(MainEffect.NavigateChat)
            markMessagesAsRead(chatMate.id)
            fetchChatHistory()
            fetchAndObserveChats()
        }
    }

    private fun sendMessage(message: Message) {
        viewModelScope.launch {
            _state.update { it ->
                it.copy(
                    currentChatHistory = it.currentChatHistory + message,
                )
            }
            val sendMessageResults =
                messageRepository.sendMessage(message.copy(status = MessageStatus.DELIVERED))
            var actionError = ""
            when (sendMessageResults) {
                is ApiResult.Success -> {
                    _state.update { currentState ->
                        val updatedHistory =
                            currentState.currentChatHistory.map { lastMessage ->
                                if (lastMessage.id == message.id) lastMessage.copy(status = MessageStatus.DELIVERED) else lastMessage
                            }
                        currentState.copy(currentChatHistory = updatedHistory)
                    }
                }

                is ApiResult.Error -> {
                    actionError = sendMessageResults.message
                    Log.d("SEND", actionError)
                }
            }
            Log.d("SEND", message.toString())
        }
    }

    private fun fetchStats() {
        viewModelScope.launch {
            val role = _state.value.currentUser!!.role
            var stats: Stats? = null

            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(stats = FetchStatus.LOADING)) }

            if (role == UserRole.ADMIN) {
                val fetchStatsResult = userRepository.getAdminStats()

                when (fetchStatsResult) {
                    is ApiResult.Success -> {
                        stats = fetchStatsResult.data
                        Log.d("STATS", fetchStatsResult.data.toString())
                    }

                    is ApiResult.Error -> Log.d("STATS", fetchStatsResult.message)
                }
            } else {
                val fetchStatsResult = userRepository.getClientFreelancerStats()

                when (fetchStatsResult) {
                    is ApiResult.Success -> {
                        stats = fetchStatsResult.data
                        Log.d("STATS", fetchStatsResult.data.toString())
                    }

                    is ApiResult.Error -> Log.d("STATS", fetchStatsResult.message)
                }
            }
            _state.update {
                it.copy(
                    stats = stats,
                    dataFetchStatus = it.dataFetchStatus.copy(stats = FetchStatus.DONE)
                )
            }
        }
    }

    private fun fetchCommissions() {
        viewModelScope.launch {
            val id = _state.value.currentUser!!.id
            val fetchCommissionsResult = commissionRepository.getCommissions(id)
            var commissions: List<Commission> = emptyList()
            var actionError = ""

            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(commissions = FetchStatus.LOADING)) }

            when (fetchCommissionsResult) {
                is ApiResult.Success -> commissions = fetchCommissionsResult.data
                is ApiResult.Error -> actionError = fetchCommissionsResult.message
            }

            _state.update {
                it.copy(
                    commissions = commissions,
                    dataFetchStatus = it.dataFetchStatus.copy(commissions = FetchStatus.DONE)
                )
            }
        }
    }

    private fun fetchFreelancers() {
        viewModelScope.launch {
            val fetchFreelancersResult = userRepository.getFreelancers()
            var freelancers: List<User> = emptyList()
            var actionError = ""

            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(freelancers = FetchStatus.LOADING)) }

            when (fetchFreelancersResult) {
                is ApiResult.Success -> freelancers = fetchFreelancersResult.data
                is ApiResult.Error -> actionError = fetchFreelancersResult.message
            }

            _state.update {
                it.copy(
                    freelancers = freelancers,
                    dataFetchStatus = it.dataFetchStatus.copy(freelancers = FetchStatus.DONE)
                )
            }
        }
    }

    private fun fetchNotifications() {
        viewModelScope.launch {
            val fetchNotifications = notificationRepository.getNotifications()
            var notifications: List<Notification> = emptyList()
            var actionError = ""

            _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(notifications = FetchStatus.LOADING)) }

            when (fetchNotifications) {
                is ApiResult.Success -> notifications = fetchNotifications.data
                is ApiResult.Error -> actionError = fetchNotifications.message
            }

            _state.update {
                it.copy(
                    notifications = notifications,
                    dataFetchStatus = it.dataFetchStatus.copy(notifications = FetchStatus.DONE)
                )
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            val logoutResult = authRepository.signOut()
            var actionError = ""

            when (logoutResult) {
                is ApiResult.Success -> {}
                is ApiResult.Error -> {
                    actionError = logoutResult.message
                }
            }

            _state.update {
                it.copy(actionError = actionError)
            }

            if (actionError.isBlank()) {
                delay(500)
                sendEffect(MainEffect.NavigateLogout)
            }
            handleIntent(MainIntent.IsLoadingChanged(false))
        }
    }

    suspend fun updateLastSeen(): Unit {
        userRepository.updateLastSeen()
    }

    suspend fun getCurrentUser(): User? {
        val result = userRepository.getCurrentUserProfile()

        _state.update { it.copy(dataFetchStatus = it.dataFetchStatus.copy(currentUser = FetchStatus.LOADING)) }

        when (result) {
            is ApiResult.Success -> {
                if (result.data.isApproved) _state.update {
                    it.copy(
                        currentUser = result.data,
                        dataFetchStatus = it.dataFetchStatus.copy(currentUser = FetchStatus.DONE)
                    )
                }
                else authRepository.signOut()
            }

            is ApiResult.Error -> Log.d("MAIN", result.message)
        }
        Log.d("MAINVIEW", _state.value.currentUser.toString())
        return _state.value.currentUser
    }

    private fun sendEffect(effect: MainEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}

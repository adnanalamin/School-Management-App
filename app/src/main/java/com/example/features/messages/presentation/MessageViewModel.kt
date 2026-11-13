package com.example.features.messages.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.features.messages.data.MessageRepository
import com.example.features.messages.domain.ChatMessageEntry
import com.example.features.messages.domain.MessageChannelType
import com.example.features.messages.domain.MessagePriority
import com.example.features.messages.domain.MessageThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class MessageUiState(
    val threads: List<MessageThread> = emptyList(),
    val searchQuery: String = "",
    val selectedChannelFilter: MessageChannelType? = null,
    val onlyUnreadFilter: Boolean = false,
    val includeArchived: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showComposeDialog: Boolean = false,
    val showThreadDialog: Boolean = false,
    val selectedThread: MessageThread? = null
) {
    val filteredThreads: List<MessageThread>
        get() = threads.filter { thread ->
            val matchesArchive = includeArchived || !thread.isArchived
            val matchesUnread = !onlyUnreadFilter || thread.unreadCount > 0
            val matchesChannel = selectedChannelFilter == null || thread.channelType == selectedChannelFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                thread.subject.lowercase().contains(q) ||
                    thread.threadCode.lowercase().contains(q) ||
                    thread.participantsLabel.lowercase().contains(q) ||
                    thread.latestMessagePreview.lowercase().contains(q)
            }
            matchesArchive && matchesUnread && matchesChannel && matchesSearch
        }.sortedWith(
            compareByDescending<MessageThread> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )

    val totalThreadsCount: Int
        get() = threads.count { !it.isArchived }

    val unreadMessagesTotal: Int
        get() = threads.filter { !it.isArchived }.sumOf { it.unreadCount }

    val pinnedCount: Int
        get() = threads.count { it.isPinned && !it.isArchived }

    val totalMessagesCount: Int
        get() = threads.sumOf { it.messageCount }
}

class MessageViewModel(
    private val messageRepository: MessageRepository = MessageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = messageRepository.getPreSeededThreads(schoolId)
        _uiState.update { it.copy(threads = seeded) }
        loadThreads()
    }

    fun loadThreads() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = messageRepository.getThreads(schoolId = schoolId)
                _uiState.update { it.copy(threads = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load messages"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setChannelFilter(channel: MessageChannelType?) {
        _uiState.update { it.copy(selectedChannelFilter = channel) }
    }

    fun toggleOnlyUnreadFilter() {
        _uiState.update { it.copy(onlyUnreadFilter = !it.onlyUnreadFilter) }
    }

    fun toggleIncludeArchived() {
        _uiState.update { it.copy(includeArchived = !it.includeArchived) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedChannelFilter = null,
                onlyUnreadFilter = false,
                includeArchived = false
            )
        }
    }

    fun openComposeDialog() {
        if (!TenantContext.hasPermission(Permission.MESSAGES_SEND)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_SEND permission") }
            return
        }
        _uiState.update { it.copy(showComposeDialog = true, errorMessage = null) }
    }

    fun closeComposeDialog() {
        _uiState.update { it.copy(showComposeDialog = false) }
    }

    fun openThread(thread: MessageThread) {
        if (!TenantContext.hasPermission(Permission.MESSAGES_VIEW)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_VIEW permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val clearedThread = thread.copy(unreadCount = 0)
        _uiState.update { state ->
            state.copy(
                threads = state.threads.map { if (it.id == thread.id) clearedThread else it },
                selectedThread = clearedThread,
                showThreadDialog = true,
                errorMessage = null
            )
        }

        if (thread.unreadCount > 0) {
            viewModelScope.launch {
                messageRepository.markThreadRead(schoolId, thread.id)
            }
        }
    }

    fun closeThread() {
        _uiState.update {
            it.copy(
                showThreadDialog = false,
                selectedThread = null
            )
        }
    }

    fun createThread(
        subject: String,
        channelType: MessageChannelType,
        priority: MessagePriority,
        participantsLabel: String,
        initialMessageBody: String
    ) {
        if (!TenantContext.hasPermission(Permission.MESSAGES_SEND)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_SEND permission") }
            return
        }

        if (subject.isBlank() || participantsLabel.isBlank() || initialMessageBody.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Subject, recipient(s), and message body are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val userId = TenantContext.activeUserId.value ?: "usr_member"
        val role = TenantContext.activeUserRole.value
        val now = System.currentTimeMillis()
        val timeLabel = "Today, ${SimpleDateFormat("hh:mm a", Locale.US).format(Date(now))}"
        val prefix = if (schoolId.contains("oxford")) "OXF" else "SCH"
        val code = "MSG-$prefix-${UUID.randomUUID().toString().take(4).uppercase()}"

        val firstMessage = ChatMessageEntry(
            id = "m_${UUID.randomUUID().toString().take(8)}",
            senderId = userId,
            senderName = currentPersonaName(role),
            senderRole = role.displayName,
            body = initialMessageBody.trim(),
            sentAtLabel = timeLabel,
            timestamp = now
        )

        val newThread = MessageThread(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            threadCode = code,
            subject = subject.trim(),
            channelType = channelType,
            priority = priority,
            participantsLabel = participantsLabel.trim(),
            participantRoles = listOf(role.name),
            messages = listOf(firstMessage),
            unreadCount = 0,
            isPinned = false,
            isArchived = false,
            lastActivityLabel = timeLabel,
            createdAt = now,
            updatedAt = now
        )

        _uiState.update { state ->
            state.copy(
                threads = listOf(newThread) + state.threads,
                showComposeDialog = false,
                statusMessage = "Secure message thread $code started.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            messageRepository.saveThread(newThread)
        }
    }

    fun sendReply(threadId: String, body: String) {
        if (!TenantContext.hasPermission(Permission.MESSAGES_SEND)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_SEND permission") }
            return
        }
        if (body.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Reply message cannot be empty.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val userId = TenantContext.activeUserId.value ?: "usr_member"
        val role = TenantContext.activeUserRole.value
        val now = System.currentTimeMillis()
        val timeLabel = "Today, ${SimpleDateFormat("hh:mm a", Locale.US).format(Date(now))}"

        val reply = ChatMessageEntry(
            id = "m_${UUID.randomUUID().toString().take(8)}",
            senderId = userId,
            senderName = currentPersonaName(role),
            senderRole = role.displayName,
            body = body.trim(),
            sentAtLabel = timeLabel,
            timestamp = now
        )

        _uiState.update { state ->
            val updatedThreads = state.threads.map { thread ->
                if (thread.id == threadId) {
                    thread.copy(
                        messages = thread.messages + reply,
                        unreadCount = 0,
                        lastActivityLabel = timeLabel,
                        updatedAt = now
                    )
                } else thread
            }
            val updatedSelected = updatedThreads.firstOrNull { it.id == threadId }
            state.copy(
                threads = updatedThreads,
                selectedThread = if (state.showThreadDialog) updatedSelected else state.selectedThread,
                statusMessage = "Reply sent securely.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            messageRepository.sendReply(schoolId, threadId, reply)
        }
    }

    fun togglePin(threadId: String) {
        if (!TenantContext.hasPermission(Permission.MESSAGES_MODERATE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_MODERATE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        var nowPinned = false
        _uiState.update { state ->
            val updatedThreads = state.threads.map { thread ->
                if (thread.id == threadId) {
                    nowPinned = !thread.isPinned
                    thread.copy(isPinned = nowPinned)
                } else thread
            }
            val updatedSelected = updatedThreads.firstOrNull { it.id == threadId }
            state.copy(
                threads = updatedThreads,
                selectedThread = if (state.showThreadDialog) updatedSelected else state.selectedThread,
                statusMessage = if (nowPinned) "Thread pinned to top." else "Thread unpinned.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            messageRepository.togglePin(schoolId, threadId)
        }
    }

    fun toggleArchive(threadId: String) {
        if (!TenantContext.hasPermission(Permission.MESSAGES_MODERATE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_MODERATE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        var nowArchived = false
        _uiState.update { state ->
            val updatedThreads = state.threads.map { thread ->
                if (thread.id == threadId) {
                    nowArchived = !thread.isArchived
                    thread.copy(isArchived = nowArchived)
                } else thread
            }
            val updatedSelected = updatedThreads.firstOrNull { it.id == threadId }
            state.copy(
                threads = updatedThreads,
                selectedThread = if (state.showThreadDialog) updatedSelected else state.selectedThread,
                statusMessage = if (nowArchived) "Thread moved to archive." else "Thread restored from archive.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            messageRepository.toggleArchive(schoolId, threadId)
        }
    }

    fun deleteThread(threadId: String) {
        if (!TenantContext.hasPermission(Permission.MESSAGES_MODERATE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires MESSAGES_MODERATE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                threads = state.threads.filterNot { it.id == threadId },
                showThreadDialog = false,
                selectedThread = null,
                statusMessage = "Conversation removed."
            )
        }

        viewModelScope.launch {
            messageRepository.deleteThread(schoolId, threadId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    private fun currentPersonaName(role: UserRole): String {
        return when (role) {
            UserRole.SUPER_ADMIN -> "Platform Super Admin"
            UserRole.SCHOOL_ADMIN -> "Dr. Robert Vance"
            UserRole.TEACHER -> "Dr. Sarah Jenkins"
            UserRole.STUDENT -> "Amina Rahman"
            UserRole.PARENT -> "David Rahman"
            UserRole.ACCOUNTANT -> "Marcus Vance"
            UserRole.LIBRARIAN -> "Eleanor Rigby"
            UserRole.TRANSPORT_MANAGER -> "Arthur Pendelton"
            UserRole.STAFF -> "Campus Staff"
        }
    }
}

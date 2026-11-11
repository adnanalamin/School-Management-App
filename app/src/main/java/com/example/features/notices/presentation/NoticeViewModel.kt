package com.example.features.notices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.notices.data.NoticeRepository
import com.example.features.notices.domain.NoticeCategory
import com.example.features.notices.domain.NoticePriority
import com.example.features.notices.domain.NoticeTargetAudience
import com.example.features.notices.domain.SchoolNotice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class NoticeUiState(
    val notices: List<SchoolNotice> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: NoticeCategory? = null,
    val selectedAudienceFilter: NoticeTargetAudience? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showCreateNoticeDialog: Boolean = false,
    val showNoticeDetailDialog: Boolean = false,
    val selectedNoticeForDetail: SchoolNotice? = null
) {
    val filteredNotices: List<SchoolNotice>
        get() = notices.filter { not ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                not.title.lowercase().contains(q) ||
                not.content.lowercase().contains(q) ||
                not.authorName.lowercase().contains(q)
            }
            val matchesCategory = selectedCategoryFilter == null || not.category == selectedCategoryFilter
            val matchesAudience = selectedAudienceFilter == null || not.targetAudience == selectedAudienceFilter || not.targetAudience == NoticeTargetAudience.ALL
            matchesSearch && matchesCategory && matchesAudience
        }.sortedWith(
            compareByDescending<SchoolNotice> { it.isPinned }
                .thenByDescending { it.priority == NoticePriority.URGENT }
                .thenByDescending { it.createdAt }
        )

    val totalNoticesCount: Int
        get() = notices.size

    val urgentNoticesCount: Int
        get() = notices.count { it.priority == NoticePriority.URGENT }

    val pinnedNoticesCount: Int
        get() = notices.count { it.isPinned }

    val activeCategoriesCount: Int
        get() = notices.map { it.category }.distinct().size
}

class NoticeViewModel(
    private val noticeRepository: NoticeRepository = NoticeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeUiState())
    val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = noticeRepository.getPreSeededNotices(schoolId)
        _uiState.update { it.copy(notices = seeded) }
        loadNotices()
    }

    fun loadNotices() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = noticeRepository.getNotices(schoolId = schoolId)
                _uiState.update { it.copy(notices = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to fetch notices"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: NoticeCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun setAudienceFilter(audience: NoticeTargetAudience?) {
        _uiState.update { it.copy(selectedAudienceFilter = audience) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedCategoryFilter = null, selectedAudienceFilter = null) }
    }

    fun openCreateNoticeDialog() {
        if (!TenantContext.hasPermission(Permission.NOTICES_PUBLISH)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires NOTICES_PUBLISH permission") }
            return
        }
        _uiState.update { it.copy(showCreateNoticeDialog = true, errorMessage = null) }
    }

    fun closeCreateNoticeDialog() {
        _uiState.update { it.copy(showCreateNoticeDialog = false) }
    }

    fun openNoticeDetail(notice: SchoolNotice) {
        _uiState.update {
            it.copy(
                selectedNoticeForDetail = notice,
                showNoticeDetailDialog = true
            )
        }
    }

    fun closeNoticeDetailDialog() {
        _uiState.update {
            it.copy(
                showNoticeDetailDialog = false,
                selectedNoticeForDetail = null
            )
        }
    }

    fun createNotice(
        title: String,
        content: String,
        category: NoticeCategory,
        priority: NoticePriority,
        targetAudience: NoticeTargetAudience,
        isPinned: Boolean,
        attachmentTitle: String
    ) {
        if (!TenantContext.hasPermission(Permission.NOTICES_PUBLISH)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires NOTICES_PUBLISH permission") }
            return
        }

        if (title.isBlank() || content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Notice title and announcement content are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val newNotice = SchoolNotice(
            id = "not_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            title = title.trim(),
            content = content.trim(),
            category = category,
            priority = priority,
            targetAudience = targetAudience,
            authorId = "usr_active_admin",
            authorName = "Administrative Directorate",
            publishedDate = dateFormat.format(Date()),
            isPinned = isPinned,
            attachmentTitle = attachmentTitle.trim()
        )

        _uiState.update { state ->
            state.copy(
                notices = listOf(newNotice) + state.notices,
                showCreateNoticeDialog = false,
                statusMessage = "Notice '${newNotice.title}' broadcasted to ${newNotice.targetAudience.displayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            noticeRepository.saveNotice(newNotice)
        }
    }

    fun togglePinNotice(noticeId: String) {
        if (!TenantContext.hasPermission(Permission.NOTICES_PUBLISH)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires NOTICES_PUBLISH permission") }
            return
        }

        val target = _uiState.value.notices.firstOrNull { it.id == noticeId } ?: return
        val updated = target.copy(isPinned = !target.isPinned)

        _uiState.update { state ->
            val updatedList = state.notices.map { if (it.id == noticeId) updated else it }
            state.copy(
                notices = updatedList,
                statusMessage = if (updated.isPinned) "Notice pinned to top of board." else "Notice unpinned."
            )
        }

        viewModelScope.launch {
            noticeRepository.saveNotice(updated)
        }
    }

    fun deleteNotice(noticeId: String) {
        if (!TenantContext.hasPermission(Permission.NOTICES_PUBLISH)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires NOTICES_PUBLISH permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                notices = state.notices.filter { it.id != noticeId },
                statusMessage = "Announcement removed from board."
            )
        }

        viewModelScope.launch {
            noticeRepository.deleteNotice(schoolId, noticeId)
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}

package com.example.features.leave.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.features.leave.data.LeaveRepository
import com.example.features.leave.domain.LeaveApplicantType
import com.example.features.leave.domain.LeaveCategory
import com.example.features.leave.domain.LeaveRequest
import com.example.features.leave.domain.LeaveStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class LeaveUiState(
    val leaveRequests: List<LeaveRequest> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: LeaveStatus? = null,
    val selectedApplicantTypeFilter: LeaveApplicantType? = null,
    val selectedCategoryFilter: LeaveCategory? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showApplyLeaveDialog: Boolean = false,
    val showReviewDialog: Boolean = false,
    val showDetailDialog: Boolean = false,
    val selectedLeaveForAction: LeaveRequest? = null
) {
    val filteredLeaveRequests: List<LeaveRequest>
        get() = leaveRequests.filter { req ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                req.applicantName.lowercase().contains(q) ||
                    req.requestCode.lowercase().contains(q) ||
                    req.departmentOrGrade.lowercase().contains(q) ||
                    req.reason.lowercase().contains(q) ||
                    req.reviewerName.lowercase().contains(q)
            }
            val matchesStatus = selectedStatusFilter == null || req.status == selectedStatusFilter
            val matchesType = selectedApplicantTypeFilter == null || req.applicantType == selectedApplicantTypeFilter
            val matchesCategory = selectedCategoryFilter == null || req.category == selectedCategoryFilter
            matchesSearch && matchesStatus && matchesType && matchesCategory
        }.sortedWith(
            compareByDescending<LeaveRequest> { it.status == LeaveStatus.PENDING }
                .thenByDescending { it.createdAt }
        )

    val totalCount: Int
        get() = leaveRequests.size

    val pendingCount: Int
        get() = leaveRequests.count { it.status == LeaveStatus.PENDING }

    val approvedCount: Int
        get() = leaveRequests.count { it.status == LeaveStatus.APPROVED }

    val totalApprovedDays: Int
        get() = leaveRequests.filter { it.status == LeaveStatus.APPROVED }.sumOf { it.totalDays }
}

class LeaveViewModel(
    private val leaveRepository: LeaveRepository = LeaveRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaveUiState())
    val uiState: StateFlow<LeaveUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = leaveRepository.getPreSeededLeaveRequests(schoolId)
        _uiState.update { it.copy(leaveRequests = seeded) }
        loadLeaveRequests()
    }

    fun loadLeaveRequests() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = leaveRepository.getLeaveRequests(schoolId = schoolId)
                _uiState.update { it.copy(leaveRequests = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load leave requests"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStatusFilter(status: LeaveStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun setApplicantTypeFilter(type: LeaveApplicantType?) {
        _uiState.update { it.copy(selectedApplicantTypeFilter = type) }
    }

    fun setCategoryFilter(category: LeaveCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedStatusFilter = null,
                selectedApplicantTypeFilter = null,
                selectedCategoryFilter = null
            )
        }
    }

    fun openApplyLeaveDialog() {
        if (!TenantContext.hasPermission(Permission.LEAVES_APPLY)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LEAVES_APPLY permission") }
            return
        }
        _uiState.update { it.copy(showApplyLeaveDialog = true, errorMessage = null) }
    }

    fun closeApplyLeaveDialog() {
        _uiState.update { it.copy(showApplyLeaveDialog = false) }
    }

    fun openReviewDialog(request: LeaveRequest) {
        if (!TenantContext.hasPermission(Permission.LEAVES_APPROVE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LEAVES_APPROVE permission") }
            return
        }
        _uiState.update {
            it.copy(
                selectedLeaveForAction = request,
                showReviewDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeReviewDialog() {
        _uiState.update {
            it.copy(
                showReviewDialog = false,
                selectedLeaveForAction = if (it.showDetailDialog) it.selectedLeaveForAction else null
            )
        }
    }

    fun openDetailDialog(request: LeaveRequest) {
        _uiState.update {
            it.copy(
                selectedLeaveForAction = request,
                showDetailDialog = true
            )
        }
    }

    fun closeDetailDialog() {
        _uiState.update {
            it.copy(
                showDetailDialog = false,
                selectedLeaveForAction = null
            )
        }
    }

    fun submitLeaveRequest(
        applicantName: String,
        applicantType: LeaveApplicantType,
        departmentOrGrade: String,
        category: LeaveCategory,
        startDate: String,
        endDate: String,
        totalDays: Int,
        reason: String,
        attachmentNote: String = ""
    ) {
        if (!TenantContext.hasPermission(Permission.LEAVES_APPLY)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LEAVES_APPLY permission") }
            return
        }

        if (applicantName.isBlank() || startDate.isBlank() || endDate.isBlank() || reason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Applicant name, dates, and reason are required.") }
            return
        }

        if (totalDays <= 0) {
            _uiState.update { it.copy(errorMessage = "Total leave days must be at least 1.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val userId = TenantContext.activeUserId.value ?: "usr_member"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())
        val codePrefix = if (schoolId.contains("oxford")) "OXF" else "SCH"
        val codeSuffix = UUID.randomUUID().toString().take(4).uppercase()

        val newRequest = LeaveRequest(
            id = "lv_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            requestCode = "LV-$codePrefix-$codeSuffix",
            applicantId = userId,
            applicantName = applicantName.trim(),
            applicantType = applicantType,
            departmentOrGrade = departmentOrGrade.ifBlank { "General" }.trim(),
            category = category,
            startDate = startDate.trim(),
            endDate = endDate.trim(),
            totalDays = totalDays,
            reason = reason.trim(),
            attachmentNote = attachmentNote.trim(),
            status = LeaveStatus.PENDING,
            submittedDate = todayStr,
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                leaveRequests = listOf(newRequest) + state.leaveRequests,
                showApplyLeaveDialog = false,
                statusMessage = "Leave request ${newRequest.requestCode} submitted for review.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            leaveRepository.saveLeaveRequest(newRequest)
        }
    }

    fun approveLeaveRequest(leaveId: String, remarks: String = "") {
        processReview(leaveId, LeaveStatus.APPROVED, remarks.ifBlank { "Approved by administration." })
    }

    fun rejectLeaveRequest(leaveId: String, remarks: String) {
        if (remarks.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please provide a reason when declining a leave request.") }
            return
        }
        processReview(leaveId, LeaveStatus.REJECTED, remarks.trim())
    }

    private fun processReview(leaveId: String, targetStatus: LeaveStatus, remarks: String) {
        if (!TenantContext.hasPermission(Permission.LEAVES_APPROVE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LEAVES_APPROVE permission") }
            return
        }

        val existing = _uiState.value.leaveRequests.firstOrNull { it.id == leaveId }
        if (existing == null) {
            _uiState.update { it.copy(errorMessage = "Leave request not found.") }
            return
        }
        if (existing.status == LeaveStatus.CANCELLED) {
            _uiState.update { it.copy(errorMessage = "Cannot review a withdrawn leave request.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val reviewer = defaultReviewerName(TenantContext.activeUserRole.value)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        _uiState.update { state ->
            val updatedList = state.leaveRequests.map { req ->
                if (req.id == leaveId) {
                    req.copy(
                        status = targetStatus,
                        reviewerName = reviewer,
                        reviewRemarks = remarks,
                        reviewedDate = todayStr
                    )
                } else req
            }
            val updatedSelected = updatedList.firstOrNull { it.id == leaveId }
            state.copy(
                leaveRequests = updatedList,
                showReviewDialog = false,
                selectedLeaveForAction = if (state.showDetailDialog) updatedSelected else null,
                statusMessage = "Leave ${existing.requestCode} marked as ${targetStatus.displayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            leaveRepository.reviewLeaveRequest(
                schoolId = schoolId,
                leaveId = leaveId,
                newStatus = targetStatus,
                reviewerName = reviewer,
                reviewRemarks = remarks,
                reviewedDate = todayStr
            )
        }
    }

    fun withdrawLeaveRequest(leaveId: String) {
        if (!TenantContext.hasPermission(Permission.LEAVES_APPLY)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LEAVES_APPLY permission") }
            return
        }

        val existing = _uiState.value.leaveRequests.firstOrNull { it.id == leaveId } ?: return
        if (!existing.canWithdraw) {
            _uiState.update { it.copy(errorMessage = "Only pending leave requests can be withdrawn.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updatedList = state.leaveRequests.map { req ->
                if (req.id == leaveId) req.copy(status = LeaveStatus.CANCELLED) else req
            }
            val updatedSelected = updatedList.firstOrNull { it.id == leaveId }
            state.copy(
                leaveRequests = updatedList,
                selectedLeaveForAction = if (state.showDetailDialog) updatedSelected else state.selectedLeaveForAction,
                statusMessage = "Leave request ${existing.requestCode} withdrawn.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            leaveRepository.withdrawLeaveRequest(schoolId, leaveId)
        }
    }

    fun deleteLeaveRequest(leaveId: String) {
        if (!TenantContext.hasPermission(Permission.LEAVES_APPROVE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires LEAVES_APPROVE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                leaveRequests = state.leaveRequests.filterNot { it.id == leaveId },
                showDetailDialog = false,
                showReviewDialog = false,
                selectedLeaveForAction = null,
                statusMessage = "Leave record removed from archive."
            )
        }

        viewModelScope.launch {
            leaveRepository.deleteLeaveRequest(schoolId, leaveId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    private fun defaultReviewerName(role: UserRole): String {
        return when (role) {
            UserRole.SUPER_ADMIN -> "Platform Super Admin"
            UserRole.SCHOOL_ADMIN -> "Dr. Robert Vance (Principal)"
            UserRole.TEACHER -> "Dr. Sarah Jenkins (Faculty)"
            else -> role.displayName
        }
    }
}

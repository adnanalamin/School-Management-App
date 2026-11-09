package com.example.features.dashboard

import androidx.lifecycle.ViewModel
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DashboardNotice(
    val id: String,
    val title: String,
    val date: String,
    val targetAudience: String,
    val isUrgent: Boolean = false
)

data class DashboardClassSession(
    val id: String,
    val subject: String,
    val timeSlot: String,
    val className: String,
    val room: String,
    val teacherName: String
)

data class DashboardUiState(
    val schoolName: String = "Oxford International Academy",
    val schoolCode: String = "OXF-101",
    val userName: String = "Dr. Robert Vance",
    val userRole: UserRole = UserRole.SCHOOL_ADMIN,
    val unreadNotifications: Int = 3,
    val totalStudents: String = "1,480",
    val totalTeachers: String = "94",
    val totalStaff: String = "32",
    val todayAttendancePercentage: String = "94.2%",
    val pendingFees: String = "$14,250",
    val todayClassesCount: Int = 6,
    val recentNotices: List<DashboardNotice> = listOf(
        DashboardNotice("n1", "Annual Science Olympiad Registration Open", "Today, 09:30 AM", "All Students", true),
        DashboardNotice("n2", "Mid-Term Examination Routine Published", "Yesterday", "Teachers & Students"),
        DashboardNotice("n3", "Parent-Teacher Conference Scheduled for Friday", "04 Nov 2026", "Parents")
    ),
    val todaySessions: List<DashboardClassSession> = listOf(
        DashboardClassSession("s1", "Advanced Physics", "08:30 - 09:15 AM", "Grade 10 - A", "Lab 2", "Dr. H. Wells"),
        DashboardClassSession("s2", "Mathematics (Calculus)", "09:20 - 10:05 AM", "Grade 10 - A", "Room 301", "Mrs. S. Roy"),
        DashboardClassSession("s3", "English Literature", "10:20 - 11:05 AM", "Grade 10 - B", "Room 204", "Mr. A. Clarke")
    ),
    val quickActionFeedback: String? = null
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        val currentRole = TenantContext.activeUserRole.value
        val name = when (currentRole) {
            UserRole.SUPER_ADMIN -> "Platform Super Admin"
            UserRole.SCHOOL_ADMIN -> "Dr. Robert Vance"
            UserRole.TEACHER -> "Sarah Jenkins (Faculty)"
            UserRole.STUDENT -> "Amina Rahman (Grade 10)"
            UserRole.PARENT -> "David Rahman (Parent)"
            UserRole.ACCOUNTANT -> "Marcus Vance (Finance)"
            else -> "Staff Member"
        }
        _uiState.update {
            it.copy(
                userRole = currentRole,
                userName = name
            )
        }
    }

    fun triggerQuickAction(actionName: String) {
        _uiState.update { it.copy(quickActionFeedback = "Action triggered: $actionName") }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(quickActionFeedback = null) }
    }

    fun hasPermission(permission: Permission): Boolean {
        return TenantContext.hasPermission(permission)
    }
}

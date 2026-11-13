package com.example.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.security.Permission
import com.example.core.security.UserRole

/**
 * Screen routes, titles, icons, and RBAC permissions for EduManage SaaS navigation.
 */
sealed class AppScreen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val requiredPermission: Permission? = null,
    val allowedRoles: Set<UserRole> = UserRole.entries.toSet()
) {
    data object Splash : AppScreen(
        route = "splash",
        title = "EduManage"
    )

    data object Onboarding : AppScreen(
        route = "onboarding",
        title = "Welcome"
    )

    data object Login : AppScreen(
        route = "login",
        title = "Sign In"
    )

    data object Register : AppScreen(
        route = "register",
        title = "Register Institution",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN)
    )

    data object MainDashboard : AppScreen(
        route = "main_dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    data object StudentsList : AppScreen(
        route = "students",
        title = "Students",
        icon = Icons.Default.School,
        requiredPermission = Permission.STUDENTS_VIEW
    )

    data object TeachersList : AppScreen(
        route = "teachers",
        title = "Faculty & Staff",
        icon = Icons.Default.Group,
        requiredPermission = Permission.TEACHERS_VIEW,
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN, UserRole.TEACHER, UserRole.STAFF)
    )

    data object Classes : AppScreen(
        route = "classes",
        title = "Classes & Routine",
        icon = Icons.Default.MeetingRoom,
        requiredPermission = Permission.CLASSES_VIEW,
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN, UserRole.TEACHER, UserRole.STUDENT)
    )

    data object Attendance : AppScreen(
        route = "attendance",
        title = "Attendance",
        icon = Icons.Default.CheckCircle,
        requiredPermission = Permission.ATTENDANCE_VIEW
    )

    data object Fees : AppScreen(
        route = "fees",
        title = "Fees",
        icon = Icons.Default.AttachMoney,
        requiredPermission = Permission.FEES_VIEW,
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.SCHOOL_ADMIN, UserRole.ACCOUNTANT, UserRole.PARENT)
    )

    data object Exams : AppScreen(
        route = "exams",
        title = "Examinations",
        icon = Icons.Default.DateRange,
        requiredPermission = Permission.EXAMS_VIEW
    )

    data object Results : AppScreen(
        route = "results",
        title = "Gradebook & Results",
        icon = Icons.Default.Assessment,
        requiredPermission = Permission.RESULTS_VIEW
    )

    data object Notices : AppScreen(
        route = "notices",
        title = "Noticeboard",
        icon = Icons.Default.Notifications,
        requiredPermission = Permission.NOTICES_VIEW
    )

    data object Homework : AppScreen(
        route = "homework",
        title = "Homework & Coursework",
        icon = Icons.Default.Assignment,
        requiredPermission = Permission.HOMEWORK_VIEW
    )

    data object Library : AppScreen(
        route = "library",
        title = "Library & Circulation",
        icon = Icons.Default.LocalLibrary,
        requiredPermission = Permission.LIBRARY_VIEW
    )

    data object Transport : AppScreen(
        route = "transport",
        title = "Transport & Fleet",
        icon = Icons.Default.DirectionsBus,
        requiredPermission = Permission.TRANSPORT_VIEW
    )

    data object Events : AppScreen(
        route = "events",
        title = "Events & Calendar",
        icon = Icons.Default.Event,
        requiredPermission = Permission.EVENTS_VIEW
    )

    data object Documents : AppScreen(
        route = "documents",
        title = "Documents & Vault",
        icon = Icons.Default.FolderShared,
        requiredPermission = Permission.DOCUMENTS_VIEW
    )

    data object Leaves : AppScreen(
        route = "leaves",
        title = "Leave & Approvals",
        icon = Icons.Default.EventBusy,
        requiredPermission = Permission.LEAVES_APPLY
    )

    data object Messages : AppScreen(
        route = "messages",
        title = "Internal Messaging",
        icon = Icons.Default.Forum,
        requiredPermission = Permission.MESSAGES_VIEW
    )

    data object Profile : AppScreen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.AccountCircle
    )

    data object Settings : AppScreen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    data object DesignSystem : AppScreen(
        route = "design_system",
        title = "Design Tokens",
        icon = Icons.Default.Palette
    )

    data object Foundation : AppScreen(
        route = "foundation",
        title = "Architecture Hub",
        icon = Icons.Default.Apartment
    )

    data object TenantArchitecture : AppScreen(
        route = "tenant_architecture",
        title = "Multi-Tenant Architecture",
        icon = Icons.Default.Apartment
    )

    data object RbacMatrix : AppScreen(
        route = "rbac_matrix",
        title = "RBAC & Permission Engine",
        icon = Icons.Default.Settings
    )

    companion object {
        fun fromRoute(route: String?): AppScreen {
            return when (route?.substringBefore("?")) {
                Splash.route -> Splash
                Onboarding.route -> Onboarding
                Login.route -> Login
                Register.route -> Register
                MainDashboard.route -> MainDashboard
                StudentsList.route -> StudentsList
                TeachersList.route -> TeachersList
                Classes.route -> Classes
                Attendance.route -> Attendance
                Fees.route -> Fees
                Exams.route -> Exams
                Results.route -> Results
                Notices.route -> Notices
                Homework.route -> Homework
                Library.route -> Library
                Transport.route -> Transport
                Events.route -> Events
                Documents.route -> Documents
                Leaves.route -> Leaves
                Messages.route -> Messages
                Profile.route -> Profile
                Settings.route -> Settings
                DesignSystem.route -> DesignSystem
                Foundation.route -> Foundation
                TenantArchitecture.route -> TenantArchitecture
                RbacMatrix.route -> RbacMatrix
                else -> MainDashboard
            }
        }
    }
}

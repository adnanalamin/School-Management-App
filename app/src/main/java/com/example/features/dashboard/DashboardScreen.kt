package com.example.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.UserRole
import com.example.core.theme.EduError
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduCard
import com.example.shared.components.EduStatCard
import com.example.shared.components.EduTenantTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToTeachers: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onNavigateToAttendance: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToExams: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
    }

    LaunchedEffect(uiState.quickActionFeedback) {
        uiState.quickActionFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("dashboard_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EduTenantTopAppBar(
                schoolName = uiState.schoolName,
                schoolCode = uiState.schoolCode,
                roleTitle = uiState.userRole.displayName,
                notificationsCount = uiState.unreadNotifications,
                onProfileClick = onNavigateToProfile,
                testTag = "dashboard_app_bar"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = EduSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
        ) {
            // Welcome Header
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${uiState.userName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Academic Session 2026-2027 • Today's Overview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    EduBadge(text = uiState.userRole.displayName, type = BadgeType.PRIMARY)
                }
            }

            // Executive Stats Grid (2x2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    EduStatCard(
                        title = "Students",
                        value = uiState.totalStudents,
                        icon = Icons.Default.School,
                        trendText = "+12% YoY",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudents,
                        testTag = "dash_stat_students"
                    )
                    EduStatCard(
                        title = "Teachers",
                        value = uiState.totalTeachers,
                        icon = Icons.Default.Group,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        trendText = "94 active",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTeachers,
                        testTag = "dash_stat_teachers"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    EduStatCard(
                        title = "Attendance",
                        value = uiState.todayAttendancePercentage,
                        icon = Icons.Default.CheckCircle,
                        iconTint = EduSuccess,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        trendText = "On Track",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAttendance,
                        testTag = "dash_stat_attendance"
                    )
                    EduStatCard(
                        title = "Pending Fees",
                        value = uiState.pendingFees,
                        icon = Icons.Default.AttachMoney,
                        iconTint = EduWarning,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        trendText = "34 invoices",
                        isPositiveTrend = false,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFees,
                        testTag = "dash_stat_fees"
                    )
                }
            }

            // Quick Actions Bar
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    if (viewModel.hasPermission(Permission.ATTENDANCE_MARK)) {
                        QuickActionButton(
                            label = "Mark Attendance",
                            icon = Icons.Default.CheckCircle,
                            onClick = onNavigateToAttendance,
                            testTag = "action_mark_attendance"
                        )
                    }
                    if (viewModel.hasPermission(Permission.STUDENTS_CREATE)) {
                        QuickActionButton(
                            label = "Enroll Student",
                            icon = Icons.Default.PersonAdd,
                            onClick = onNavigateToStudents,
                            testTag = "action_enroll_student"
                        )
                    }
                    if (viewModel.hasPermission(Permission.TEACHERS_CREATE)) {
                        QuickActionButton(
                            label = "Add Faculty",
                            icon = Icons.Default.Group,
                            onClick = onNavigateToTeachers,
                            testTag = "action_add_faculty"
                        )
                    }
                    if (viewModel.hasPermission(Permission.CLASSES_VIEW)) {
                        QuickActionButton(
                            label = "Class Routine",
                            icon = Icons.Default.MeetingRoom,
                            onClick = onNavigateToClasses,
                            testTag = "action_class_routine"
                        )
                    }
                    if (viewModel.hasPermission(Permission.FEES_COLLECT)) {
                        QuickActionButton(
                            label = "Collect Fee",
                            icon = Icons.Default.AttachMoney,
                            onClick = onNavigateToFees,
                            testTag = "action_collect_fee"
                        )
                    }
                    if (viewModel.hasPermission(Permission.EXAMS_VIEW)) {
                        QuickActionButton(
                            label = "Exam Schedule",
                            icon = Icons.Default.DateRange,
                            onClick = onNavigateToExams,
                            testTag = "action_exam_schedule"
                        )
                    }
                    if (viewModel.hasPermission(Permission.HOMEWORK_CREATE)) {
                        QuickActionButton(
                            label = "Assign Homework",
                            icon = Icons.Default.Assignment,
                            onClick = { viewModel.triggerQuickAction("New Assignment") },
                            testTag = "action_new_homework"
                        )
                    }
                    if (viewModel.hasPermission(Permission.NOTICES_PUBLISH)) {
                        QuickActionButton(
                            label = "Post Notice",
                            icon = Icons.Default.Notifications,
                            onClick = { viewModel.triggerQuickAction("Publish Notice") },
                            testTag = "action_post_notice"
                        )
                    }
                }
            }

            // Today's Timetable Preview
            item {
                Text(
                    text = "Today's Academic Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.todaySessions) { session ->
                ClassSessionRow(session)
            }

            // Recent Notices
            item {
                Text(
                    text = "Recent School Notices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.recentNotices) { notice ->
                NoticeCard(notice)
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.lg))
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ClassSessionRow(session: DashboardClassSession) {
    EduCard(testTag = "session_${session.id}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.className} • ${session.room} • ${session.teacherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = session.timeSlot,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NoticeCard(notice: DashboardNotice) {
    EduCard(testTag = "notice_${notice.id}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notice.isUrgent) {
                        EduBadge(text = "URGENT", type = BadgeType.ERROR)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = notice.targetAudience,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notice.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

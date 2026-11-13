package com.example.features.leave.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.leave.domain.LeaveApplicantType
import com.example.features.leave.domain.LeaveRequest
import com.example.features.leave.domain.LeaveStatus
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    viewModel: LeaveViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("leaves_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leave & Approval Workflow",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tenant: ${(TenantContext.activeSchoolId.value ?: "").take(15)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_leaves_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.LEAVES_APPLY)) {
                        IconButton(
                            onClick = { viewModel.openApplyLeaveDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_apply_leave")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Submit Leave Request")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EduStatCard(
                        title = "Total Requests",
                        value = "${uiState.totalCount}",
                        icon = Icons.Default.EventBusy,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(155.dp)
                    )
                    EduStatCard(
                        title = "Pending Review",
                        value = "${uiState.pendingCount}",
                        icon = Icons.Default.HourglassTop,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.width(155.dp)
                    )
                    EduStatCard(
                        title = "Approved",
                        value = "${uiState.approvedCount}",
                        icon = Icons.Default.CheckCircle,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.width(155.dp)
                    )
                    EduStatCard(
                        title = "Approved Days",
                        value = "${uiState.totalApprovedDays}d",
                        icon = Icons.Default.EventAvailable,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.width(155.dp)
                    )
                }
            }

            // Search Input
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search applicant, request code, grade, or reason...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_leaves")
                )
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All Statuses") },
                        modifier = Modifier.testTag("chip_leave_status_all")
                    )
                    LeaveStatus.entries.forEach { status ->
                        FilterChip(
                            selected = uiState.selectedStatusFilter == status,
                            onClick = {
                                if (uiState.selectedStatusFilter == status) {
                                    viewModel.setStatusFilter(null)
                                } else {
                                    viewModel.setStatusFilter(status)
                                }
                            },
                            label = { Text(status.displayName) },
                            modifier = Modifier.testTag("chip_leave_status_${status.name.lowercase()}")
                        )
                    }
                }
            }

            // Applicant Type Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedApplicantTypeFilter == null,
                        onClick = { viewModel.setApplicantTypeFilter(null) },
                        label = { Text("All Roles") },
                        modifier = Modifier.testTag("chip_leave_role_all")
                    )
                    LeaveApplicantType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedApplicantTypeFilter == type,
                            onClick = {
                                if (uiState.selectedApplicantTypeFilter == type) {
                                    viewModel.setApplicantTypeFilter(null)
                                } else {
                                    viewModel.setApplicantTypeFilter(type)
                                }
                            },
                            label = { Text(type.displayName) },
                            modifier = Modifier.testTag("chip_leave_role_${type.name.lowercase()}")
                        )
                    }
                }
            }

            // Loading indicator
            item {
                AnimatedVisibility(visible = uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty state
            if (!uiState.isLoading && uiState.filteredLeaveRequests.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching leave requests",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clear your filters or tap + to submit a new leave application.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Leave Requests List
            items(uiState.filteredLeaveRequests, key = { it.id }) { leave ->
                LeaveRequestCard(
                    leaveRequest = leave,
                    onInspect = { viewModel.openDetailDialog(leave) },
                    onReview = { viewModel.openReviewDialog(leave) },
                    onWithdraw = { viewModel.withdrawLeaveRequest(leave.id) },
                    onDelete = { viewModel.deleteLeaveRequest(leave.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showApplyLeaveDialog) {
        ApplyLeaveDialog(
            onDismiss = { viewModel.closeApplyLeaveDialog() },
            onConfirm = { name, type, dept, cat, start, end, days, reason, attachment ->
                viewModel.submitLeaveRequest(name, type, dept, cat, start, end, days, reason, attachment)
            }
        )
    }

    if (uiState.showReviewDialog && uiState.selectedLeaveForAction != null) {
        val target = uiState.selectedLeaveForAction!!
        ReviewLeaveDialog(
            leaveRequest = target,
            onDismiss = { viewModel.closeReviewDialog() },
            onApprove = { remarks -> viewModel.approveLeaveRequest(target.id, remarks) },
            onReject = { remarks -> viewModel.rejectLeaveRequest(target.id, remarks) }
        )
    }

    if (uiState.showDetailDialog && uiState.selectedLeaveForAction != null) {
        val target = uiState.selectedLeaveForAction!!
        LeaveDetailDialog(
            leaveRequest = target,
            onDismiss = { viewModel.closeDetailDialog() },
            onOpenReview = { viewModel.openReviewDialog(target) },
            onWithdraw = { viewModel.withdrawLeaveRequest(target.id) }
        )
    }
}

@Composable
fun LeaveRequestCard(
    leaveRequest: LeaveRequest,
    onInspect: () -> Unit,
    onReview: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColors = when (leaveRequest.status) {
        LeaveStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        LeaveStatus.APPROVED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        LeaveStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        LeaveStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("leave_card_${leaveRequest.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = leaveRequest.applicantName.firstOrNull()?.uppercase() ?: "L",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = leaveRequest.applicantName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${leaveRequest.requestCode} • ${leaveRequest.applicantType.displayName} • ${leaveRequest.departmentOrGrade}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColors.first)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = leaveRequest.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColors.second
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${leaveRequest.startDate} → ${leaveRequest.endDate} (${leaveRequest.totalDays}d)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = leaveRequest.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = leaveRequest.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            if (leaveRequest.reviewerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reviewed by ${leaveRequest.reviewerName}: ${leaveRequest.reviewRemarks}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_inspect_leave_${leaveRequest.id}")
                ) {
                    Text("Inspect", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (leaveRequest.isPending && TenantContext.hasPermission(Permission.LEAVES_APPROVE)) {
                        Button(
                            onClick = onReview,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_review_leave_${leaveRequest.id}")
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Review", fontSize = 12.sp)
                        }
                    } else if (leaveRequest.canWithdraw && TenantContext.hasPermission(Permission.LEAVES_APPLY)) {
                        OutlinedButton(
                            onClick = onWithdraw,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_withdraw_leave_${leaveRequest.id}")
                        ) {
                            Text("Withdraw", fontSize = 12.sp)
                        }
                    }

                    if (TenantContext.hasPermission(Permission.LEAVES_APPROVE)) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_delete_leave_${leaveRequest.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Leave Request",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

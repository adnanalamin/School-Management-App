package com.example.features.rbac

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.security.Permission
import com.example.core.security.UserRole
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RbacMatrixScreen(
    viewModel: RbacMatrixViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.notificationFeedback) {
        uiState.notificationFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("rbac_matrix_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RBAC & Permission Engine (Phase 7)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_rbac_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = EduSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
        ) {
            // Header Hero Card
            item {
                EduCard(testTag = "card_rbac_hero") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.size(EduSpacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "9-Role Security Architecture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Granular least-privilege matrix governing 36 system permissions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        EduBadge(text = "STRICT RBAC", type = BadgeType.PRIMARY)
                    }
                }
            }

            // Active Session Banner & Role Switcher
            item {
                EduCard(testTag = "card_active_session") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Active Session Role",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = uiState.activeSessionRole.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        EduBadge(text = "Level ${uiState.activeSessionRole.level}", type = BadgeType.INFO)
                    }

                    if (uiState.inspectedRole != uiState.activeSessionRole) {
                        Spacer(modifier = Modifier.height(EduSpacing.xs))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(EduSpacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Test app as ${uiState.inspectedRole.displayName}:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            EduButton(
                                text = "Assume Role in Session",
                                onClick = { viewModel.assumeRoleForActiveSession(uiState.inspectedRole) },
                                variant = EduButtonVariant.PRIMARY,
                                testTag = "btn_assume_role"
                            )
                        }
                    }
                }
            }

            // Role Selector Chips (All 9 Roles)
            item {
                Text(
                    text = "Select Role to Inspect & Audit (9 Roles)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("row_role_selector_chips"),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                ) {
                    uiState.allRoles.forEach { role ->
                        val isSelected = role == uiState.inspectedRole
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectInspectedRole(role) },
                            label = { Text("${role.displayName} (${role.level})") },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_role_${role.name}")
                        )
                    }
                }
            }

            // Inspected Role Summary Card
            item {
                val summary = uiState.roleSummary
                EduCard(testTag = "card_role_summary") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = summary.role.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = summary.primaryDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        EduBadge(
                            text = "${summary.permissionCount} / ${summary.totalAvailablePermissions} Perms",
                            type = if (summary.permissionCount > 20) BadgeType.SUCCESS else BadgeType.PRIMARY
                        )
                    }
                }
            }

            // Interactive Action Authorization Simulator
            item {
                Text(
                    text = "Live Action Authorization Simulator",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduCard(testTag = "card_action_simulator") {
                    Text(
                        text = "Simulate executing a privileged action as '${uiState.inspectedRole.displayName}':",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(EduSpacing.xs))

                    // Test actions carousel
                    val sampleActions = listOf(
                        Permission.FEES_COLLECT to "Collect Tuition Fee",
                        Permission.STUDENTS_DELETE to "Archive Student Record",
                        Permission.RESULTS_PUBLISH to "Publish Board Exam Results",
                        Permission.ATTENDANCE_MARK to "Take Daily Attendance",
                        Permission.NOTICES_PUBLISH to "Post Urgent Notice",
                        Permission.TEACHERS_CREATE to "Hire New Teacher",
                        Permission.TRANSPORT_MANAGE to "Reassign Bus Route",
                        Permission.LIBRARY_MANAGE to "Issue Library Book",
                        Permission.HOMEWORK_SUBMIT to "Submit Homework Assignment"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("row_sample_actions"),
                        horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                    ) {
                        sampleActions.forEach { (perm, label) ->
                            val isSelected = perm == uiState.testActionPermission
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSimulationPermission(perm) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("action_chip_${perm.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(EduSpacing.sm))

                    // Simulation Result Banner
                    uiState.simulationResult?.let { result ->
                        val isAllowed = result.isAllowed
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isAllowed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = EduShapes.small
                                )
                                .padding(EduSpacing.sm)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (isAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.size(EduSpacing.xs))
                                Column {
                                    Text(
                                        text = if (isAllowed) "ACCESS GRANTED" else "ACCESS BLOCKED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllowed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = result.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isAllowed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.testTag("txt_simulation_result_reason")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search and Category Filters for Permissions
            item {
                Text(
                    text = "Permission Matrix Audit (${viewModel.getFilteredPermissions().size} items)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().testTag("input_search_permissions"),
                    placeholder = { Text("Search by permission key or description...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = EduShapes.medium,
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All Domains") },
                        modifier = Modifier.testTag("chip_cat_all")
                    )
                    uiState.categories.forEach { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("chip_cat_$cat")
                        )
                    }
                }
            }

            // Permissions List for Inspected Role
            val filteredPermissions = viewModel.getFilteredPermissions()
            items(filteredPermissions) { perm ->
                val isGranted = uiState.inspectedRolePermissions.contains(perm)
                EduCard(testTag = "card_perm_${perm.name}") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = perm.key,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = perm.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        EduBadge(
                            text = if (isGranted) "AUTHORIZED" else "DENIED",
                            type = if (isGranted) BadgeType.SUCCESS else BadgeType.ERROR
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xl))
            }
        }
    }
}

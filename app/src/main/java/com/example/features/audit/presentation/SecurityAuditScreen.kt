package com.example.features.audit.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.features.audit.domain.AuditCategory
import com.example.features.audit.domain.AuditSeverity
import com.example.features.audit.domain.SecurityAuditCheck
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SecurityAuditScreen(
    viewModel: SecurityAuditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val msg = uiState.errorMessage ?: uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("security_audit_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Security Audit & Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Last run ${uiState.lastRunTimestampLabel} • ${uiState.summary.passedChecks}/${uiState.summary.totalChecks} passing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_audit_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
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
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                EduCard(testTag = "card_audit_summary") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(EduSpacing.md))
                            Column {
                                Text(
                                    text = "Compliance Score: ${uiState.summary.scorePercent}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.summary.passedChecks} Passed • ${uiState.summary.warningChecks} Warnings • ${uiState.summary.criticalChecks} Critical",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        EduBadge(
                            text = "Grade ${uiState.summary.gradeLabel}",
                            type = if (uiState.summary.criticalChecks == 0) BadgeType.SUCCESS else BadgeType.ERROR
                        )
                    }

                    Spacer(modifier = Modifier.height(EduSpacing.md))
                    EduButton(
                        text = "Re-Run Security & Benchmark Suite",
                        onClick = { viewModel.runFullAudit(showFeedback = true) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        variant = EduButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_run_security_audit"
                    )
                }
            }

            item {
                val m = uiState.performanceMetrics
                EduCard(testTag = "card_performance_metrics") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Performance & Architecture Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(EduSpacing.sm))
                    MetricLine("RBAC Lookup (1,000 evals)", "${"%.2f".format(m.rbacEval1kTimeMs)} ms")
                    MetricLine("Tenant Boundary Check (1,000 ops)", "${"%.2f".format(m.tenantBoundary1kTimeMs)} ms")
                    MetricLine("Input Sanitizer (1,000 ops)", "${"%.2f".format(m.validation1kTimeMs)} ms")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    MetricLine("Keyed Lazy Lists Optimized", "${m.lazyKeyedListsVerified} lists")
                    MetricLine("Protected Navigation Routes", "${m.protectedRoutesCount} screens")
                    MetricLine("Categorized RBAC Permissions", "${m.categorizedPermissionsCount} permissions")
                }
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All (${uiState.checks.size})") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("audit_filter_ALL")
                    )
                    AuditCategory.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.displayName) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("audit_filter_${category.name}")
                        )
                    }
                }
            }

            items(uiState.filteredChecks, key = { it.id }) { check ->
                AuditCheckCard(check = check)
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.lg))
            }
        }
    }
}

@Composable
private fun MetricLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AuditCheckCard(check: SecurityAuditCheck) {
    val isPass = check.severity == AuditSeverity.PASS
    EduCard(testTag = "audit_check_${check.id}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = check.severity.label,
                    tint = if (isPass) EduSuccess else EduWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = check.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            EduBadge(
                text = check.severity.label,
                type = if (isPass) BadgeType.SUCCESS else BadgeType.WARNING
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = check.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

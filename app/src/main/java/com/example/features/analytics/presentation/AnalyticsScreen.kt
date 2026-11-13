package com.example.features.analytics.presentation

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.School
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
import com.example.features.analytics.domain.AnalyticsChartPoint
import com.example.features.analytics.domain.AnalyticsDomain
import com.example.features.analytics.domain.ExecutiveReportSnapshot
import com.example.features.analytics.domain.ReportPeriod
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val bundle = uiState.bundle

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
        modifier = modifier.fillMaxSize().testTag("analytics_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Executive Reports & Analytics",
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
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_analytics_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.ANALYTICS_EXPORT)) {
                        IconButton(
                            onClick = { viewModel.openGenerateReportDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_generate_report")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Generate Report")
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
            // Period Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = uiState.selectedPeriod == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period.displayName) },
                            modifier = Modifier.testTag("chip_period_${period.name.lowercase()}")
                        )
                    }
                }
            }

            // Domain Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalyticsDomain.entries.forEach { domain ->
                        FilterChip(
                            selected = uiState.selectedDomain == domain,
                            onClick = { viewModel.selectDomain(domain) },
                            label = { Text(domain.displayName) },
                            modifier = Modifier.testTag("chip_domain_${domain.name.lowercase()}")
                        )
                    }
                }
            }

            // KPI Summary Strip
            if (bundle != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EduStatCard(
                            title = "Avg Attendance",
                            value = "${bundle.averageAttendancePct}%",
                            icon = Icons.Default.CheckCircle,
                            iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.width(155.dp)
                        )
                        EduStatCard(
                            title = "Fee Collection",
                            value = "${bundle.feeCollectionRatePct}%",
                            icon = Icons.Default.AttachMoney,
                            iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.width(155.dp)
                        )
                        EduStatCard(
                            title = "Academic GPA",
                            value = "${bundle.averageGpa}",
                            icon = Icons.Default.School,
                            iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.width(155.dp)
                        )
                        EduStatCard(
                            title = "Fleet / Library",
                            value = "${bundle.fleetUtilizationPct.toInt()}% / ${bundle.libraryUtilizationPct.toInt()}%",
                            icon = Icons.Default.DirectionsBus,
                            iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.width(165.dp)
                        )
                    }
                }

                // Visualizations filtered by selected domain
                if (uiState.selectedDomain == AnalyticsDomain.OVERVIEW || uiState.selectedDomain == AnalyticsDomain.ATTENDANCE) {
                    item {
                        BarTrendChartCard(
                            title = "Weekly Attendance Trend",
                            subtitle = "${bundle.schoolName} • ${bundle.period.displayName}",
                            points = bundle.attendanceTrend,
                            maxScale = 100f,
                            testTag = "chart_attendance_trend"
                        )
                    }
                }

                if (uiState.selectedDomain == AnalyticsDomain.OVERVIEW || uiState.selectedDomain == AnalyticsDomain.FINANCE) {
                    item {
                        val maxRev = (bundle.revenueBreakdown.maxOfOrNull { it.value } ?: 30000f) * 1.15f
                        BarTrendChartCard(
                            title = "Tuition Collections vs. Outstanding",
                            subtitle = "Collected ${bundle.collectedRevenueLabel} • Pending ${bundle.pendingRevenueLabel}",
                            points = bundle.revenueBreakdown,
                            maxScale = maxRev,
                            showSecondarySeries = true,
                            primaryLegend = "Collected",
                            secondaryLegend = "Outstanding",
                            testTag = "chart_revenue_breakdown"
                        )
                    }
                }

                if (uiState.selectedDomain == AnalyticsDomain.OVERVIEW || uiState.selectedDomain == AnalyticsDomain.ACADEMICS) {
                    item {
                        HorizontalDistributionCard(
                            title = "Academic Grade & GPA Distribution",
                            subtitle = "Pass Rate ${bundle.passRatePct}% • ${bundle.totalStudents} Enrolled Learners",
                            points = bundle.gpaDistribution,
                            testTag = "chart_gpa_distribution"
                        )
                    }
                }

                if (uiState.selectedDomain == AnalyticsDomain.OPERATIONS) {
                    item {
                        HorizontalDistributionCard(
                            title = "Campus Operations Utilization",
                            subtitle = "${bundle.pendingLeavesCount} Pending Leave Applications • ${bundle.activeFaculty} Active Faculty",
                            points = listOf(
                                AnalyticsChartPoint("Library Circulation Rate", bundle.libraryUtilizationPct, unitSuffix = "%"),
                                AnalyticsChartPoint("Transport Fleet Occupancy", bundle.fleetUtilizationPct, unitSuffix = "%"),
                                AnalyticsChartPoint("Daily Attendance Readiness", bundle.averageAttendancePct, unitSuffix = "%")
                            ),
                            testTag = "chart_operations_utilization"
                        )
                    }
                }

                item {
                    DepartmentScorecardCard(
                        rows = bundle.departmentMetrics,
                        testTag = "scorecard_departments"
                    )
                }
            }

            // Loading indicator
            item {
                AnimatedVisibility(visible = uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Exportable Executive Report Snapshots Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Executive Reports (${uiState.filteredReports.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (TenantContext.hasPermission(Permission.ANALYTICS_EXPORT)) {
                        OutlinedButton(
                            onClick = { viewModel.openGenerateReportDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_inline_generate_report")
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Report", fontSize = 12.sp)
                        }
                    }
                }
            }

            items(uiState.filteredReports, key = { it.id }) { report ->
                ExecutiveReportCard(
                    report = report,
                    onPreview = { viewModel.openReportPreview(report) },
                    onDelete = { viewModel.deleteReport(report.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (uiState.showGenerateReportDialog) {
        GenerateReportDialog(
            initialDomain = uiState.selectedDomain,
            initialPeriod = uiState.selectedPeriod,
            onDismiss = { viewModel.closeGenerateReportDialog() },
            onConfirm = { title, domain, period, format, notes ->
                viewModel.generateReport(title, domain, period, format, notes)
            }
        )
    }

    if (uiState.showReportPreviewDialog && uiState.selectedReportForPreview != null) {
        ReportPreviewDialog(
            report = uiState.selectedReportForPreview!!,
            csvPayload = uiState.exportedCsvPreview,
            onDismiss = { viewModel.closeReportPreview() }
        )
    }
}

@Composable
fun ExecutiveReportCard(
    report: ExecutiveReportSnapshot,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("report_card_${report.id}")
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = report.domain.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${report.reportCode} • ${report.generatedDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = report.exportFormat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = report.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = report.headlineKpi,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
            if (report.summaryNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.summaryNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
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
                    onClick = onPreview,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_preview_report_${report.id}")
                ) {
                    Text("Preview & CSV", fontSize = 12.sp)
                }

                if (TenantContext.hasPermission(Permission.ANALYTICS_EXPORT)) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_delete_report_${report.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Report",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

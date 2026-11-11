package com.example.features.results.presentation

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduElevation
import com.example.core.theme.EduError
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.results.domain.ResultStatus
import com.example.features.results.domain.StudentResult
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hasEnterPermission = TenantContext.hasPermission(Permission.RESULTS_ENTER)
    val hasPublishPermission = TenantContext.hasPermission(Permission.RESULTS_PUBLISH)

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessages()
        }
    }

    Scaffold(
        modifier = modifier.testTag("result_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gradebook & Academic Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
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
                        modifier = Modifier.testTag("result_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasPublishPermission) {
                        EduButton(
                            text = "Publish All",
                            onClick = { viewModel.publishAllPendingResults() },
                            variant = EduButtonVariant.OUTLINED,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("publish_results_button")
                        )
                    }
                    if (hasEnterPermission) {
                        EduButton(
                            text = "+ Score Sheet",
                            onClick = { viewModel.openScoreEntryDialog() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("record_score_button")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = EduSpacing.md)
        ) {
            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // 4 Executive Stat Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                EduStatCard(
                    title = "Candidates",
                    value = uiState.totalResultsCount.toString(),
                    subtitle = "Marked records",
                    icon = Icons.Default.Grading,
                    modifier = Modifier.weight(1f)
                )
                EduStatCard(
                    title = "Class Average",
                    value = "${String.format("%.1f", uiState.classAveragePercentage)}%",
                    subtitle = "Cohort mean",
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                EduStatCard(
                    title = "Top Distinction",
                    value = "${String.format("%.1f", uiState.highestPercentage)}%",
                    subtitle = "Highest score",
                    icon = Icons.Default.EmojiEvents,
                    modifier = Modifier.weight(1f)
                )
                EduStatCard(
                    title = "Pass Rate",
                    value = "${String.format("%.1f", uiState.passRatePercentage)}%",
                    subtitle = "Passed >=40%",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(EduSpacing.md))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_search_input"),
                placeholder = { Text("Search by candidate name, roll number, or subject...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = EduShapes.medium
            )

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedStatusFilter == null,
                    onClick = { viewModel.setStatusFilter(null) },
                    label = { Text("All Results") },
                    colors = FilterChipDefaults.filterChipColors(),
                    modifier = Modifier.testTag("result_chip_all")
                )
                ResultStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.selectedStatusFilter == status,
                        onClick = { viewModel.setStatusFilter(if (uiState.selectedStatusFilter == status) null else status) },
                        label = { Text(status.displayName) },
                        colors = FilterChipDefaults.filterChipColors(),
                        modifier = Modifier.testTag("result_chip_${status.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.md))

            // Results List
            if (uiState.isLoading && uiState.results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No Examination Results Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "Try adjusting your search criteria" else "Click '+ Score Sheet' to enter student scores",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    items(
                        items = uiState.filteredResults,
                        key = { it.id }
                    ) { result ->
                        StudentResultCard(
                            result = result,
                            onViewReportCard = { viewModel.openReportCard(result) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showScoreEntryDialog) {
        ScoreEntryDialog(
            onDismiss = { viewModel.closeScoreEntryDialog() },
            onConfirm = { name, roll, grade, section, exam, session, scores, remarks ->
                viewModel.addStudentResult(
                    studentName = name,
                    rollNumber = roll,
                    gradeLevel = grade,
                    section = section,
                    examTitle = exam,
                    academicSession = session,
                    scores = scores,
                    teacherRemarks = remarks
                )
            }
        )
    }

    if (uiState.showReportCardDialog && uiState.generatedReportCard != null) {
        ReportCardDialog(
            reportCard = uiState.generatedReportCard!!,
            onDismiss = { viewModel.closeReportCardDialog() }
        )
    }
}

@Composable
fun StudentResultCard(
    result: StudentResult,
    onViewReportCard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_card_${result.id}"),
        shape = EduShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EduElevation.level2)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Candidate Info & Honors / Grade Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${result.rollNumber} • ${result.gradeLevel} (${result.section})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = result.examTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Rank & Grade Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (result.rankInClass > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Rank #${result.rankInClass}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EduSuccessContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.overallGrade,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = EduSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score Metrics Row: Total Marks, Percentage, GPA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(EduShapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Obtained", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${result.totalObtained.toInt()} / ${result.totalMaxMarks.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Percentage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.2f", result.percentage)}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cumulative GPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.2f", result.gpa)} / 4.0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject Score Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                result.subjectScores.forEach { sub ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${sub.subjectCode}: ${sub.marksObtained.toInt()} (${sub.grade})",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status: ${result.status.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (result.status == ResultStatus.PUBLISHED) EduSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                )

                EduButton(
                    text = "Report Card Transcript",
                    onClick = onViewReportCard,
                    variant = EduButtonVariant.OUTLINED,
                    leadingIcon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                    modifier = Modifier.testTag("view_report_card_button_${result.id}")
                )
            }
        }
    }
}

package com.example.features.tenant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TenantArchitectureScreen(
    viewModel: TenantArchitectureViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("tenant_architecture_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Multi-Tenant Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("tenant_arch_back_btn")) {
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
            // Header explanation
            item {
                EduCard(testTag = "card_multi_tenant_intro") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Phase 6 SaaS Tenant Isolation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        EduBadge(text = "ZERO LEAKAGE", type = BadgeType.SUCCESS)
                    }
                    Spacer(modifier = Modifier.height(EduSpacing.xs))
                    Text(
                        text = "Every educational institution is isolated under schools/{schoolId}. Subcollections for students, teachers, fees, and exams can never be accessed cross-tenant.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tenant Switcher (Test School A vs School B)
            item {
                Text(
                    text = "Select Active School Tenant to Test Isolation:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    uiState.availableTenants.forEach { tenant ->
                        val isSelected = tenant.id == uiState.activeTenantId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.switchActiveTenant(tenant.id) }
                                .padding(10.dp)
                                .testTag("tenant_tab_${tenant.code}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tenant.code,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tenant.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = tenant.location,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Path & Firewall Live Telemetry
            item {
                EduCard(testTag = "card_firestore_telemetry") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = EduSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tenant Security Boundary",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        EduBadge(text = uiState.firewallStatus, type = BadgeType.SUCCESS)
                    }

                    Spacer(modifier = Modifier.height(EduSpacing.xs))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(EduSpacing.xs))

                    Text(
                        text = "Active Firestore Scoped Path:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.currentPath,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 2.dp).testTag("text_firestore_scoped_path")
                    )
                }
            }

            // Subcollections Selector (All 15 modules)
            item {
                Text(
                    text = "15 Isolated Sub-Collections:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.subcollectionsList.forEach { colName ->
                        val isSelected = colName == uiState.selectedSubcollection
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.selectSubcollection(colName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("subcol_chip_$colName")
                        ) {
                            Text(
                                text = colName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Isolated Data Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Isolated Documents (${uiState.totalRecordsInSubcollection} items)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tenant: ${uiState.activeTenantId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Render documents based on active subcollection
            when (uiState.selectedSubcollection) {
                "students" -> {
                    items(uiState.students, key = { it.id }) { student ->
                        DocumentRow(
                            title = student.name,
                            subtitle = "Roll: ${student.rollNumber} • ${student.className} (${student.section})",
                            meta = "Guardian: ${student.parentEmail}",
                            badge = student.status,
                            testTag = "item_student_${student.id}"
                        )
                    }
                }
                "teachers" -> {
                    items(uiState.teachers, key = { it.id }) { teacher ->
                        DocumentRow(
                            title = teacher.name,
                            subtitle = "ID: ${teacher.employeeId} • ${teacher.subject}",
                            meta = teacher.email,
                            badge = teacher.designation,
                            testTag = "item_teacher_${teacher.id}"
                        )
                    }
                }
                "fees" -> {
                    items(uiState.fees, key = { it.id }) { fee ->
                        DocumentRow(
                            title = fee.title,
                            subtitle = "Student: ${fee.studentName} • Due: ${fee.dueDate}",
                            meta = "Amount: $${fee.amount}",
                            badge = fee.status,
                            testTag = "item_fee_${fee.id}"
                        )
                    }
                }
                "notices" -> {
                    items(uiState.notices, key = { it.id }) { notice ->
                        DocumentRow(
                            title = notice.title,
                            subtitle = notice.content,
                            meta = "Published: ${notice.date} • Audience: ${notice.targetAudience}",
                            badge = if (notice.isUrgent) "URGENT" else "NOTICE",
                            testTag = "item_notice_${notice.id}"
                        )
                    }
                }
                else -> {
                    item {
                        EduCard(testTag = "card_generic_subcollection") {
                            Text(
                                text = "Sub-collection '${uiState.selectedSubcollection}' Schema Scoped",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All documents in '${uiState.selectedSubcollection}' are strictly partitioned at '${uiState.currentPath}' and filtered by the tenant barrier.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Cross-Tenant Breach Attack Simulation
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                EduCard(testTag = "card_attack_simulation") {
                    Text(
                        text = "Verify Cross-School Security Firewall",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trigger a simulated cross-tenant query attempting to read another school's private subcollection while authenticated as the current school.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(EduSpacing.sm))

                    EduButton(
                        text = "Simulate Cross-Tenant Access Attack",
                        onClick = { viewModel.simulateCrossTenantBreachAttempt() },
                        variant = EduButtonVariant.DANGER,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_simulate_attack"
                    )

                    if (uiState.simulatedAttackMessage != null) {
                        Spacer(modifier = Modifier.height(EduSpacing.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (uiState.isAttackBlocked) EduSuccess.copy(alpha = 0.12f)
                                    else EduError.copy(alpha = 0.12f)
                                )
                                .padding(10.dp)
                                .testTag("box_attack_result")
                        ) {
                            Text(
                                text = uiState.simulatedAttackMessage.orEmpty(),
                                color = if (uiState.isAttackBlocked) EduSuccess else EduError,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(EduSpacing.xxl))
            }
        }
    }
}

@Composable
private fun DocumentRow(
    title: String,
    subtitle: String,
    meta: String,
    badge: String,
    testTag: String
) {
    EduCard(testTag = testTag) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = meta, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
            EduBadge(text = badge, type = BadgeType.PRIMARY)
        }
    }
}

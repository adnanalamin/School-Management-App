package com.example.features.foundation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.UserRole
import com.example.core.theme.EduError
import com.example.core.theme.EduInfo
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard
import com.example.shared.components.EduTextField
import com.example.shared.models.TenantSchool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationScreen(
    viewModel: FoundationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "EduManage Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "EduManage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Multi-School SaaS Foundation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface
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
        ) {
            // Tenant Bar
            TenantSelectorBar(
                schools = uiState.availableSchools,
                selectedSchool = uiState.currentSchool,
                onSelectSchool = { viewModel.selectSchool(it.id) }
            )

            // Tabs
            val tabs = listOf(
                "Architecture" to Icons.Default.Apartment,
                "RBAC Security" to Icons.Default.Security,
                "Design Tokens" to Icons.Default.Palette,
                "Roadmap" to Icons.Default.Timeline
            )

            PrimaryTabRow(
                selectedTabIndex = uiState.activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = uiState.activeTab == index,
                        onClick = { viewModel.setTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (uiState.activeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }

            // Tab Content
            when (uiState.activeTab) {
                0 -> ArchitectureTabContent(uiState)
                1 -> RbacTabContent(
                    selectedRole = uiState.selectedRole,
                    grantedPermissions = uiState.grantedPermissions,
                    onSelectRole = { viewModel.selectRole(it) }
                )
                2 -> DesignSystemTabContent(
                    textInput = uiState.testInputText,
                    textError = uiState.testInputError,
                    onTextChange = { viewModel.onTestInputChanged(it) }
                )
                3 -> RoadmapTabContent()
            }
        }
    }
}

@Composable
private fun TenantSelectorBar(
    schools: List<TenantSchool>,
    selectedSchool: TenantSchool?,
    onSelectSchool: (TenantSchool) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EduSuccess)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVE TENANT ISOLATION BOUNDARY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }
            selectedSchool?.let {
                EduBadge(
                    text = "${it.subscriptionPlan} PLAN",
                    type = BadgeType.PRIMARY
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            schools.forEach { school ->
                val isSelected = school.id == selectedSchool?.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onSelectSchool(school) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("school_selector_${school.code}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = school.code,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = school.currency,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitectureTabContent(uiState: FoundationUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EduCard(testTag = "card_tenant_overview") {
                Text(
                    text = "Current Tenant Context",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                uiState.currentSchool?.let { school ->
                    Text(
                        text = school.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tenant ID: ${school.id}  •  Code: ${school.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Session: ${school.academicSession}  •  Type: ${school.schoolType}  •  Currency: ${school.currency}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Isolation Guarantee:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    EduBadge(text = "ZERO CROSS-TENANT LEAK", type = BadgeType.SUCCESS)
                }
            }
        }

        item {
            EduCard(testTag = "card_environment_config") {
                Text(
                    text = "Environment & Build Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                ConfigRow("Environment Profile", uiState.appConfig.environment.name)
                ConfigRow("Application Version", uiState.appConfig.appVersion)
                ConfigRow("Build Number", uiState.appConfig.buildNumber.toString())
                ConfigRow("Network Timeout", "${uiState.appConfig.apiTimeoutSeconds}s")
                ConfigRow("Strict Isolation Mode", uiState.appConfig.isMultiTenantStrictIsolationEnabled.toString())
                ConfigRow("Offline Sync Enabled", uiState.appConfig.allowOfflineSync.toString())
                ConfigRow("Supported Locales", uiState.appConfig.supportedLanguages.joinToString(", ").uppercase())
            }
        }

        item {
            EduCard(testTag = "card_firestore_structure") {
                Text(
                    text = "Multi-Tenant Firestore Hierarchy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All school data is strictly compartmentalized under schools/{schoolId}/ sub-collections:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                val subCollections = listOf(
                    "students" to "Individual enrollment, guardian link, roll number",
                    "teachers" to "Faculty profile, class allocations, subjects",
                    "parents" to "Family linking, multiple children association",
                    "classes" to "Grade levels, shifts, assigned class teachers",
                    "attendance" to "Daily morning/subject attendance registers",
                    "fees" to "Invoices, payment transactions, fee head rules",
                    "exams" to "Schedules, grading systems, report cards",
                    "notices" to "Role-targeted emergency & regular notices"
                )

                subCollections.forEach { (sub, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• schools/{id}/$sub",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.width(180.dp)
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RbacTabContent(
    selectedRole: UserRole,
    grantedPermissions: Set<Permission>,
    onSelectRole: (UserRole) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Select User Role to Inspect Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val isSelected = role == selectedRole
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onSelectRole(role) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("role_chip_${role.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            EduCard(testTag = "card_role_summary") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedRole.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RBAC Security Level: ${selectedRole.level}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    EduBadge(
                        text = "${grantedPermissions.size} GRANTED",
                        type = BadgeType.SUCCESS
                    )
                }
            }
        }

        item {
            Text(
                text = "Granular Permissions (${grantedPermissions.size}/${Permission.entries.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(Permission.entries.toList()) { permission ->
            val isGranted = grantedPermissions.contains(permission)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isGranted) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isGranted) EduSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = permission.key,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGranted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = permission.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                EduBadge(
                    text = if (isGranted) "ALLOW" else "DENIED",
                    type = if (isGranted) BadgeType.SUCCESS else BadgeType.ERROR
                )
            }
        }
    }
}

@Composable
private fun DesignSystemTabContent(
    textInput: String,
    textError: String?,
    onTextChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EduCard(testTag = "card_color_palette") {
                Text(
                    text = "Material 3 Semantic Color Tokens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                ColorSwatchRow("Primary (Navy / Indigo)", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                ColorSwatchRow("Secondary (Teal / Cyan)", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
                ColorSwatchRow("Tertiary (Amber Accent)", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
                ColorSwatchRow("Success Indicator", EduSuccess, Color.White)
                ColorSwatchRow("Warning Indicator", EduWarning, Color.White)
                ColorSwatchRow("Error Indicator", EduError, Color.White)
            }
        }

        item {
            EduCard(testTag = "card_button_variants") {
                Text(
                    text = "EduButton Reusable Components",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                EduButton(
                    text = "Primary Button (48dp Touch Target)",
                    onClick = {},
                    variant = EduButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                EduButton(
                    text = "Secondary Button",
                    onClick = {},
                    variant = EduButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                EduButton(
                    text = "Outlined Button",
                    onClick = {},
                    variant = EduButtonVariant.OUTLINED,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                EduButton(
                    text = "Danger / Destructive Action",
                    onClick = {},
                    variant = EduButtonVariant.DANGER,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                EduButton(
                    text = "Loading State",
                    onClick = {},
                    isLoading = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            EduCard(testTag = "card_text_input_demo") {
                Text(
                    text = "EduTextField & Validation State",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                EduTextField(
                    value = textInput,
                    onValueChange = onTextChange,
                    label = "Test Student Admission No.",
                    placeholder = "Enter at least 3 characters",
                    errorMessage = textError,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "test_input_admission"
                )
            }
        }
    }
}

@Composable
private fun RoadmapTabContent() {
    val phases = listOf(
        1 to ("Phase 1: Project Foundation" to "Architecture, Theme, M3 tokens, Multi-Tenant models & RBAC matrix (COMPLETED)"),
        2 to ("Phase 2: Design System" to "Buttons, Cards, Dialogs, BottomSheets, Snackbars, Charts & Theme Mode"),
        3 to ("Phase 3: App Navigation" to "GoRouter / Navigation Compose, Splash, Onboarding, Route Guards"),
        4 to ("Phase 4: Authentication" to "Email/Password, Google Sign-In, Session persistence, Error boundaries"),
        5 to ("Phase 5: School Registration" to "Multi-tenant onboarding, school code generation, configuration"),
        6 to ("Phase 6: Multi-Tenant Architecture" to "Firestore isolation verification, strict school document partitioning"),
        7 to ("Phase 7: Role & Permission System" to "Granular dynamic policies, zero-trust repository validation"),
        8 to ("Phase 8: School Admin Dashboard" to "Metrics, quick action FABs, attendance & fees summary widgets"),
        9 to ("Phase 9: Student Management" to "Enrollment, profiles, photo capture, search, filtering, CSV export"),
        10 to ("Phase 10: Parent Portal" to "Multi-child link, real-time notices, fees, homework & attendance")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "EduManage SaaS Master Roadmap (30 Phases)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Strict phase-by-phase execution per master development prompt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(phases) { (num, info) ->
            val isCurrent = num == 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) EduSuccess else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$num",
                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.first,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = info.second,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isCurrent) {
                    EduBadge(text = "ACTIVE", type = BadgeType.SUCCESS)
                }
            }
        }
    }
}

@Composable
private fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
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
private fun ColorSwatchRow(name: String, color: Color, onColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sample",
                color = onColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

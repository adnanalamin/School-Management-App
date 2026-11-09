package com.example.features.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.core.security.UserRole
import com.example.core.theme.EduError
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("profile_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "User Profile & Security",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("profile_back_btn")) {
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
            // Profile Card Header
            item {
                EduCard(testTag = "card_profile_header") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.name.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(EduSpacing.sm))
                        Text(
                            text = uiState.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(EduSpacing.sm))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EduBadge(text = uiState.role.displayName, type = BadgeType.PRIMARY)
                            Spacer(modifier = Modifier.width(EduSpacing.xs))
                            EduBadge(text = "LEVEL ${uiState.role.level}", type = BadgeType.INFO)
                        }
                    }
                }
            }

            // Tenant Affiliation
            item {
                EduCard(testTag = "card_school_affiliation") {
                    Text(
                        text = "School Tenant Affiliation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(EduSpacing.sm))

                    AffiliationRow("Institution", uiState.schoolName)
                    AffiliationRow("Tenant Code", uiState.schoolCode)
                    AffiliationRow("Academic Session", uiState.academicSession)
                    AffiliationRow("Member Since", uiState.memberSince)
                }
            }

            // Permissions Matrix Summary
            item {
                EduCard(testTag = "card_permissions_summary") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assigned Permissions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        EduBadge(text = "${uiState.permissions.size} GRANTED", type = BadgeType.SUCCESS)
                    }
                    Spacer(modifier = Modifier.height(EduSpacing.sm))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.permissions.take(8).forEach { perm ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = perm.key,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Role Switcher (For Evaluation Testing)
            item {
                EduButton(
                    text = "Switch Active Role (Test RBAC)",
                    onClick = { viewModel.openRoleSwitchDialog() },
                    variant = EduButtonVariant.OUTLINED,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_switch_role_dialog"
                )
            }

            // Sign Out
            item {
                EduButton(
                    text = "Sign Out of Workspace",
                    onClick = { viewModel.signOut(onSignedOut) },
                    variant = EduButtonVariant.DANGER,
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Default.Logout, contentDescription = null, tint = Color.White)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_sign_out"
                )
                Spacer(modifier = Modifier.height(EduSpacing.lg))
            }
        }
    }

    // Role Switch Dialog
    if (uiState.showRoleSwitchDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeRoleSwitchDialog() },
            title = {
                Text(
                    text = "Select Persona to Test Route Guards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
                    UserRole.entries.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (role == uiState.role) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { viewModel.switchRole(role) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = role.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (role == uiState.role) FontWeight.Bold else FontWeight.Normal
                            )
                            EduBadge(text = "L${role.level}", type = BadgeType.PRIMARY)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.closeRoleSwitchDialog() }) {
                    Text("Close")
                }
            },
            shape = EduShapes.large,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun AffiliationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

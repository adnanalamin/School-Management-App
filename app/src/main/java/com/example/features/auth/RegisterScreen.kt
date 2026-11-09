package com.example.features.auth

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.EduError
import com.example.core.theme.EduSpacing
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard
import com.example.shared.components.EduDropdown
import com.example.shared.components.EduPasswordInput
import com.example.shared.components.EduTextField
import com.example.shared.components.GoogleSignInButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("register_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Register Institution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("register_back_button")) {
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
            item {
                EduCard(testTag = "card_register_intro") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New School SaaS Tenant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        EduBadge(text = "PRO PLAN TRIAL", type = BadgeType.SUCCESS)
                    }
                    Spacer(modifier = Modifier.height(EduSpacing.xs))
                    Text(
                        text = "Set up your school domain, unique school code, and primary administrator credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Google SSO fast track
            item {
                GoogleSignInButton(
                    onAuthSuccess = onRegisterSuccess,
                    onAuthError = { viewModel.onSchoolCodeChanged(uiState.schoolCode) },
                    schoolId = "sch_new_institution",
                    testTag = "register_google_btn"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = EduSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = " OR REGISTER MANUALLY ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = EduSpacing.xs)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            item {
                Text(
                    text = "Institution Details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduTextField(
                    value = uiState.schoolName,
                    onValueChange = { viewModel.onSchoolNameChanged(it) },
                    label = "School / College Full Name",
                    placeholder = "e.g. Cambridge International School",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    testTag = "input_reg_school_name"
                )
            }

            item {
                EduTextField(
                    value = uiState.schoolCode,
                    onValueChange = { viewModel.onSchoolCodeChanged(it) },
                    label = "Unique School Code (3-10 chars)",
                    placeholder = "e.g. CIS101",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    testTag = "input_reg_school_code"
                )
            }

            item {
                EduDropdown(
                    label = "School Type",
                    options = uiState.availableSchoolTypes,
                    selectedOption = uiState.schoolType,
                    onOptionSelected = { viewModel.onSchoolTypeSelected(it) },
                    testTag = "dropdown_reg_school_type"
                )
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(
                    text = "Primary Administrator Account",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduTextField(
                    value = uiState.adminName,
                    onValueChange = { viewModel.onAdminNameChanged(it) },
                    label = "Administrator Full Name",
                    placeholder = "e.g. Dr. Robert Vance",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    testTag = "input_reg_admin_name"
                )
            }

            item {
                EduTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = "Administrator Work Email",
                    placeholder = "principal@cis101.edu",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    testTag = "input_reg_email"
                )
            }

            item {
                EduTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.onPhoneChanged(it) },
                    label = "Phone Number",
                    placeholder = "+1 555 123 4567",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    testTag = "input_reg_phone"
                )
            }

            item {
                EduPasswordInput(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = "Master Account Password",
                    testTag = "input_reg_password"
                )
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = EduError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("reg_error_text")
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                EduButton(
                    text = "Provision School & Workspace",
                    onClick = { viewModel.registerSchool(onSuccess = onRegisterSuccess) },
                    isLoading = uiState.isLoading,
                    variant = EduButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_submit_registration"
                )
                Spacer(modifier = Modifier.height(EduSpacing.xxl))
            }
        }
    }
}

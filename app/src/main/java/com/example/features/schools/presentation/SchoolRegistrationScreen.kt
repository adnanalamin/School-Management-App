package com.example.features.schools.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.shared.components.EduDropdown
import com.example.shared.components.EduPasswordInput
import com.example.shared.components.EduTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolRegistrationScreen(
    viewModel: SchoolRegistrationViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val stepTitles = listOf("Identity", "Contact", "Operations", "Admin")

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("school_registration_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "School Onboarding & Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentStep > 0) {
                                viewModel.previousStep()
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("school_reg_back_btn")
                    ) {
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
            // Stepper indicator
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        stepTitles.forEachIndexed { index, title ->
                            val isCompleted = index < uiState.currentStep
                            val isCurrent = index == uiState.currentStep
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> EduSuccess
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(EduSpacing.sm))
                    LinearProgressIndicator(
                        progress = { (uiState.currentStep + 1) / 4f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Live Tenant Badge & Unique ID Preview Card
            item {
                EduCard(testTag = "card_tenant_preview") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.schoolName.ifBlank { "New Educational Institution" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tenant Slug: ${uiState.schoolCode.ifBlank { "PENDING" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        EduBadge(text = uiState.schoolType, type = BadgeType.PRIMARY)
                    }

                    Spacer(modifier = Modifier.height(EduSpacing.xs))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(EduSpacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Generated School ID:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.previewSchoolId.ifBlank { "sch_code_..." },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // STEP 0: Institution Identity & Core Profile
            if (uiState.currentStep == 0) {
                item {
                    Text(
                        text = "Step 1: Institutional Identity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Specify your school name, unique institutional identifier, and education category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    EduTextField(
                        value = uiState.schoolName,
                        onValueChange = { viewModel.onSchoolNameChanged(it) },
                        label = "School Full Legal Name *",
                        placeholder = "e.g. Oxford International Academy",
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
                        label = "Unique School Code (3-10 chars) *",
                        placeholder = "e.g. OXF-101",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_school_code"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.slogan,
                        onValueChange = { viewModel.onSloganChanged(it) },
                        label = "Institutional Motto / Slogan (Optional)",
                        placeholder = "e.g. Inspiring Excellence Every Day",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_slogan"
                    )
                }

                item {
                    EduDropdown(
                        label = "School Category / Type *",
                        options = viewModel.availableSchoolTypes,
                        selectedOption = uiState.schoolType,
                        onOptionSelected = { viewModel.onSchoolTypeSelected(it) },
                        testTag = "dropdown_reg_school_type"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.establishedYear,
                        onValueChange = { viewModel.onEstablishedYearChanged(it) },
                        label = "Established Year (1800-2026) *",
                        placeholder = "e.g. 2005",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_established_year"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.logoUrl,
                        onValueChange = { viewModel.onLogoUrlChanged(it) },
                        label = "Institutional Logo URL (Optional)",
                        placeholder = "https://example.com/logo.png",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_logo_url"
                    )
                }
            }

            // STEP 1: Contact & Physical Location
            if (uiState.currentStep == 1) {
                item {
                    Text(
                        text = "Step 2: Campus Contact & Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Official administrative address and contact channels for students and guardians.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    EduTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.onAddressChanged(it) },
                        label = "Campus Street Address *",
                        placeholder = "e.g. 742 Evergreen Terrace, Educational District",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_address"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.onPhoneChanged(it) },
                        label = "Official Contact Telephone *",
                        placeholder = "e.g. +1 (555) 234-5678",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_phone"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = "Official Institutional Email *",
                        placeholder = "e.g. contact@oxfordacademy.edu",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_email"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.website,
                        onValueChange = { viewModel.onWebsiteChanged(it) },
                        label = "Official Website URL",
                        placeholder = "e.g. https://oxfordacademy.edu",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_website"
                    )
                }
            }

            // STEP 2: Academic & Regional Settings
            if (uiState.currentStep == 2) {
                item {
                    Text(
                        text = "Step 3: Academic Operations & Region",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Configure session duration, tuition currency, and operational timezone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    EduDropdown(
                        label = "Active Academic Session *",
                        options = viewModel.availableAcademicSessions,
                        selectedOption = uiState.academicSession,
                        onOptionSelected = { viewModel.onAcademicSessionSelected(it) },
                        testTag = "dropdown_reg_academic_session"
                    )
                }

                item {
                    EduDropdown(
                        label = "Financial Currency *",
                        options = viewModel.availableCurrencies.map { it.first },
                        selectedOption = uiState.currency,
                        onOptionSelected = { viewModel.onCurrencySelected(it) },
                        testTag = "dropdown_reg_currency"
                    )
                }

                item {
                    EduDropdown(
                        label = "School Operational Timezone *",
                        options = viewModel.availableTimezones,
                        selectedOption = uiState.timezone,
                        onOptionSelected = { viewModel.onTimezoneSelected(it) },
                        testTag = "dropdown_reg_timezone"
                    )
                }
            }

            // STEP 3: Administrator Account & Final Review
            if (uiState.currentStep == 3) {
                item {
                    Text(
                        text = "Step 4: Primary Administrator & Launch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Establish the root School Administrator account for this tenant workspace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    EduTextField(
                        value = uiState.adminName,
                        onValueChange = { viewModel.onAdminNameChanged(it) },
                        label = "Administrator Full Name *",
                        placeholder = "e.g. Dr. Robert Vance",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_admin_name"
                    )
                }

                item {
                    EduTextField(
                        value = uiState.adminEmail,
                        onValueChange = { viewModel.onAdminEmailChanged(it) },
                        label = "Administrator Work Email *",
                        placeholder = "e.g. principal@oxfordacademy.edu",
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        testTag = "input_reg_admin_email"
                    )
                }

                item {
                    EduPasswordInput(
                        value = uiState.adminPassword,
                        onValueChange = { viewModel.onAdminPasswordChanged(it) },
                        label = "Master Account Password *",
                        testTag = "input_reg_admin_password"
                    )
                }

                // Summary verification card
                item {
                    EduCard(testTag = "card_registration_summary") {
                        Text(
                            text = "Institutional Tenant Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(EduSpacing.xs))
                        SummaryItem("School Name", uiState.schoolName)
                        SummaryItem("Code & Identifier", "${uiState.schoolCode} (${uiState.previewSchoolId})")
                        SummaryItem("Category & Established", "${uiState.schoolType} (Est. ${uiState.establishedYear})")
                        SummaryItem("Contact Phone", uiState.phone)
                        SummaryItem("Official Email", uiState.email)
                        SummaryItem("Session & Currency", "${uiState.academicSession} • ${uiState.currencyCode} (${uiState.currencySymbol})")
                        SummaryItem("Timezone", uiState.timezone)
                    }
                }
            }

            // Error banner
            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = EduError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth().testTag("school_reg_error_text")
                    )
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    if (uiState.currentStep > 0) {
                        EduButton(
                            text = "Back",
                            onClick = { viewModel.previousStep() },
                            variant = EduButtonVariant.OUTLINED,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_reg_prev_step"
                        )
                    }

                    if (uiState.currentStep < 3) {
                        EduButton(
                            text = "Continue",
                            onClick = { viewModel.nextStep() },
                            variant = EduButtonVariant.PRIMARY,
                            modifier = Modifier.weight(if (uiState.currentStep > 0) 1f else 2f),
                            trailingIcon = {
                                Icon(imageVector = Icons.AutoMirrored.Default.ArrowForward, contentDescription = null, tint = Color.White)
                            },
                            testTag = "btn_reg_next_step"
                        )
                    } else {
                        EduButton(
                            text = "Provision School & Launch",
                            onClick = {
                                viewModel.submitRegistration(context, onSuccess = onRegistrationSuccess)
                            },
                            isLoading = uiState.isLoading,
                            variant = EduButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_reg_submit_final"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(EduSpacing.xxl))
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

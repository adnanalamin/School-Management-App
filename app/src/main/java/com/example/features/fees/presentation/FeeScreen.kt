package com.example.features.fees.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduElevation
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.fees.domain.FeeInvoice
import com.example.features.fees.domain.InvoiceStatus
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduStatCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeScreen(
    viewModel: FeeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasCreatePermission = TenantContext.hasPermission(Permission.FEES_CREATE)
    val hasCollectPermission = TenantContext.hasPermission(Permission.FEES_COLLECT)

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("fee_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fee & Invoicing Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Institutional billing & collections",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("fee_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (hasCreatePermission) {
                        EduButton(
                            text = "Issue Invoice",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = { viewModel.openCreateInvoiceDialog() },
                            modifier = Modifier.padding(end = EduSpacing.sm),
                            testTag = "open_create_invoice_button"
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
            // 1. Executive Stat Cards
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    EduStatCard(
                        title = "Total Invoiced",
                        value = "${uiState.currencySymbol}${String.format(Locale.US, "%,.0f", uiState.totalBilled)}",
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f),
                        subtitle = "${uiState.invoices.size} Active Invoices",
                        testTag = "stat_card_total_billed"
                    )
                    EduStatCard(
                        title = "Collected",
                        value = "${uiState.currencySymbol}${String.format(Locale.US, "%,.0f", uiState.totalCollected)}",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        iconTint = EduSuccess,
                        iconContainerColor = EduSuccessContainer,
                        trendText = "Cleared funds",
                        isPositiveTrend = true,
                        testTag = "stat_card_total_collected"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    EduStatCard(
                        title = "Outstanding",
                        value = "${uiState.currencySymbol}${String.format(Locale.US, "%,.0f", uiState.totalOutstanding)}",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f),
                        iconTint = Color(0xFFE65100),
                        iconContainerColor = Color(0xFFFFE0B2),
                        subtitle = "Pending settlements",
                        testTag = "stat_card_total_outstanding"
                    )
                    EduStatCard(
                        title = "Overdue",
                        value = "${uiState.overdueCount}",
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f),
                        iconTint = EduError,
                        iconContainerColor = EduErrorContainer,
                        trendText = if (uiState.overdueCount > 0) "Immediate action" else "Zero past due",
                        isPositiveTrend = uiState.overdueCount == 0,
                        testTag = "stat_card_overdue_count"
                    )
                }
            }

            // 2. Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.onStatusFilterChange(null) },
                        label = { Text("All (${uiState.invoices.size})") },
                        modifier = Modifier.testTag("filter_status_all"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    InvoiceStatus.entries.forEach { status ->
                        val isSelected = uiState.selectedStatusFilter == status
                        val count = uiState.invoices.count { it.status == status }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusFilterChange(status) },
                            label = { Text("${status.displayName} ($count)") },
                            modifier = Modifier.testTag("filter_status_${status.name.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            // 3. Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fee_search_input"),
                    placeholder = { Text("Search by invoice #, student name, roll #...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = EduShapes.medium
                )
            }

            // 4. Invoices List
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EduSpacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.filteredInvoices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EduSpacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No fee invoices found matching current criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.filteredInvoices, key = { it.id }) { invoice ->
                    InvoiceCard(
                        invoice = invoice,
                        currencySymbol = uiState.currencySymbol,
                        canCollect = hasCollectPermission,
                        onCollectPayment = { viewModel.openPaymentDialog(invoice) },
                        onViewReceipt = { viewModel.openReceiptDialog(invoice) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xxl))
            }
        }
    }

    // Modal Dialogs
    if (uiState.showCreateInvoiceDialog) {
        CreateInvoiceDialog(
            currencySymbol = uiState.currencySymbol,
            onConfirm = { name, roll, grade, sec, term, due, tuition, lab, lib, trn, spt, disc ->
                viewModel.createInvoice(
                    studentId = "",
                    studentName = name,
                    rollNumber = roll,
                    gradeLevel = grade,
                    section = sec,
                    academicTerm = term,
                    dueDate = due,
                    tuitionFee = tuition,
                    labFee = lab,
                    libraryFee = lib,
                    transportFee = trn,
                    sportsFee = spt,
                    discountAmount = disc
                )
            },
            onDismiss = { viewModel.closeCreateInvoiceDialog() }
        )
    }

    if (uiState.showPaymentDialog && uiState.selectedInvoiceForPayment != null) {
        RecordPaymentDialog(
            invoice = uiState.selectedInvoiceForPayment!!,
            currencySymbol = uiState.currencySymbol,
            onConfirm = { amount, method, notes ->
                viewModel.recordPayment(
                    invoiceId = uiState.selectedInvoiceForPayment!!.id,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes
                )
            },
            onDismiss = { viewModel.closePaymentDialog() }
        )
    }

    if (uiState.showReceiptDialog && uiState.selectedInvoiceForReceipt != null) {
        FeeReceiptDialog(
            invoice = uiState.selectedInvoiceForReceipt!!,
            transaction = uiState.selectedTransactionForReceipt,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { viewModel.closeReceiptDialog() }
        )
    }
}

@Composable
private fun InvoiceCard(
    invoice: FeeInvoice,
    currencySymbol: String,
    canCollect: Boolean,
    onCollectPayment: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val (statusBg, statusText, statusColor) = when (invoice.status) {
        InvoiceStatus.PAID -> Triple(EduSuccessContainer, "PAID", EduSuccess)
        InvoiceStatus.PARTIAL -> Triple(Color(0xFFFFE0B2), "PARTIAL", Color(0xFFE65100))
        InvoiceStatus.PENDING -> Triple(MaterialTheme.colorScheme.primaryContainer, "PENDING", MaterialTheme.colorScheme.primary)
        InvoiceStatus.OVERDUE -> Triple(EduErrorContainer, "OVERDUE", EduError)
        InvoiceStatus.CANCELLED -> Triple(MaterialTheme.colorScheme.surfaceVariant, "CANCELLED", MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_card_${invoice.id}"),
        shape = EduShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = EduElevation.level1)
    ) {
        Column(modifier = Modifier.padding(EduSpacing.md)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.xs))

            // Student details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = invoice.studentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${invoice.gradeLevel} - ${invoice.section} • Roll: ${invoice.rollNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Due: ${invoice.dueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (invoice.status == InvoiceStatus.OVERDUE) EduError else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = invoice.academicTerm,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // Financial Summary Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = EduShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EduSpacing.md, vertical = EduSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Invoiced",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.totalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.paidAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EduSuccess
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Balance Due",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.dueAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.dueAmount > 0.0) EduError else EduSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                if (invoice.transactions.isNotEmpty()) {
                    EduButton(
                        text = "Receipt (${invoice.transactions.size})",
                        variant = EduButtonVariant.OUTLINED,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = onViewReceipt,
                        modifier = Modifier.weight(1f),
                        testTag = "view_receipt_button_${invoice.id}"
                    )
                }

                if (canCollect && invoice.dueAmount > 0.0) {
                    EduButton(
                        text = "Collect Payment",
                        variant = EduButtonVariant.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = onCollectPayment,
                        modifier = Modifier.weight(1.2f),
                        testTag = "collect_payment_button_${invoice.id}"
                    )
                }
            }
        }
    }
}

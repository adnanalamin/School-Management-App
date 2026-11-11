package com.example.features.fees.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.fees.domain.FeeInvoice
import com.example.features.fees.domain.InvoiceStatus
import com.example.features.fees.domain.PaymentMethod
import com.example.features.fees.domain.PaymentTransaction
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceDialog(
    currencySymbol: String,
    onConfirm: (
        studentName: String,
        rollNumber: String,
        gradeLevel: String,
        section: String,
        academicTerm: String,
        dueDate: String,
        tuition: Double,
        lab: Double,
        library: Double,
        transport: Double,
        sports: Double,
        discount: Double
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("Grade 10") }
    var section by remember { mutableStateOf("Section A") }
    var academicTerm by remember { mutableStateOf("Term 1 (Fall 2026)") }
    var dueDate by remember { mutableStateOf("2026-04-15") }

    var tuitionInput by remember { mutableStateOf("1200") }
    var labInput by remember { mutableStateOf("200") }
    var libraryInput by remember { mutableStateOf("100") }
    var transportInput by remember { mutableStateOf("0") }
    var sportsInput by remember { mutableStateOf("0") }
    var discountInput by remember { mutableStateOf("0") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val tuition = tuitionInput.toDoubleOrNull() ?: 0.0
    val lab = labInput.toDoubleOrNull() ?: 0.0
    val library = libraryInput.toDoubleOrNull() ?: 0.0
    val transport = transportInput.toDoubleOrNull() ?: 0.0
    val sports = sportsInput.toDoubleOrNull() ?: 0.0
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val netTotal = (tuition + lab + library + transport + sports - discount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_invoice_dialog"),
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Generate Fee Invoice",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Issue fee bill to enrolled student",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EduErrorContainer),
                        shape = EduShapes.small
                    ) {
                        Text(
                            text = validationError!!,
                            color = EduError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(EduSpacing.sm)
                        )
                    }
                }

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Full Name *") },
                    placeholder = { Text("e.g. Amina Rahman") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_student_name_input"),
                    singleLine = true,
                    shape = EduShapes.medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll Number") },
                        placeholder = { Text("OXF-10A-001") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_roll_number_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade Level") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_grade_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    OutlinedTextField(
                        value = academicTerm,
                        onValueChange = { academicTerm = it },
                        label = { Text("Academic Term") },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("invoice_term_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_due_date_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = EduSpacing.xs))

                Text(
                    text = "Fee Components Breakdown ($currencySymbol)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    OutlinedTextField(
                        value = tuitionInput,
                        onValueChange = { tuitionInput = it },
                        label = { Text("Tuition Fee *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_tuition_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                    OutlinedTextField(
                        value = labInput,
                        onValueChange = { labInput = it },
                        label = { Text("Lab & STEM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_lab_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    OutlinedTextField(
                        value = libraryInput,
                        onValueChange = { libraryInput = it },
                        label = { Text("Library") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_library_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                    OutlinedTextField(
                        value = transportInput,
                        onValueChange = { transportInput = it },
                        label = { Text("Bus Transport") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_transport_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    OutlinedTextField(
                        value = sportsInput,
                        onValueChange = { sportsInput = it },
                        label = { Text("Sports") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_sports_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("Scholarship / Waiver") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_discount_input"),
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = EduShapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EduSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Invoice Total",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", netTotal)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Generate Invoice",
                onClick = {
                    if (studentName.trim().isBlank()) {
                        validationError = "Student Name is required."
                    } else if (tuition <= 0.0) {
                        validationError = "Tuition Fee must be greater than zero."
                    } else if (dueDate.trim().isBlank()) {
                        validationError = "Due Date is required."
                    } else {
                        onConfirm(
                            studentName.trim(),
                            rollNumber.trim(),
                            gradeLevel.trim(),
                            section.trim(),
                            academicTerm.trim(),
                            dueDate.trim(),
                            tuition,
                            lab,
                            library,
                            transport,
                            sports,
                            discount
                        )
                    }
                },
                testTag = "submit_generate_invoice_button"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_invoice_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    invoice: FeeInvoice,
    currencySymbol: String,
    onConfirm: (amount: Double, method: PaymentMethod, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", invoice.dueAmount)) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    var notesInput by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("record_payment_dialog"),
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EduSuccessContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = EduSuccess,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Collect Fee Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${invoice.invoiceNumber} • ${invoice.studentName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EduErrorContainer),
                        shape = EduShapes.small
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = EduError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(EduSpacing.sm)
                        )
                    }
                }

                // Balance summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EduSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Due Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.dueAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EduError
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Invoiced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.totalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Payment Amount ($currencySymbol) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    singleLine = true,
                    shape = EduShapes.medium
                )

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMethod.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .testTag("payment_method_dropdown"),
                        shape = EduShapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        PaymentMethod.entries.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.displayName) },
                                onClick = {
                                    selectedMethod = method
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Transaction Notes / Reference") },
                    placeholder = { Text("e.g. Bank ACH Ref #987654") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_notes_input"),
                    shape = EduShapes.medium
                )
            }
        },
        confirmButton = {
            EduButton(
                text = "Confirm & Issue Receipt",
                onClick = {
                    val amt = amountInput.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than zero."
                    } else if (amt > invoice.dueAmount) {
                        errorMessage = "Payment amount cannot exceed remaining due balance."
                    } else {
                        onConfirm(amt, selectedMethod, notesInput.trim())
                    }
                },
                testTag = "confirm_payment_button"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_payment_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FeeReceiptDialog(
    invoice: FeeInvoice,
    transaction: PaymentTransaction?,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    val tx = transaction ?: invoice.transactions.lastOrNull()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("fee_receipt_dialog"),
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Receipt Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(EduSpacing.sm))
                        Column {
                            Text(
                                text = "EduManage Official Receipt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Institution Accounts Office",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (invoice.dueAmount <= 0.0) EduSuccessContainer else MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (invoice.dueAmount <= 0.0) "PAID IN FULL" else "PARTIAL INSTALLMENT",
                            color = if (invoice.dueAmount <= 0.0) EduSuccess else MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = EduSpacing.md))

                // Receipt metadata
                ReceiptRow("Receipt Number", tx?.receiptNumber ?: "RCP-PROVISIONAL")
                ReceiptRow("Invoice Number", invoice.invoiceNumber)
                ReceiptRow("Student Name", invoice.studentName)
                ReceiptRow("Roll Number", invoice.rollNumber)
                ReceiptRow("Grade & Section", "${invoice.gradeLevel} - ${invoice.section}")
                ReceiptRow("Academic Term", invoice.academicTerm)
                ReceiptRow("Payment Date", dateFormat.format(Date(tx?.paymentDate ?: System.currentTimeMillis())))
                ReceiptRow("Payment Method", tx?.paymentMethod?.displayName ?: "Direct Counter")
                ReceiptRow("Processed By", tx?.recordedByName ?: "Accounts Officer")

                HorizontalDivider(modifier = Modifier.padding(vertical = EduSpacing.md))

                // Payment Figures Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(EduSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Invoice Amount", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Amount Paid This Transaction",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EduSuccess
                            )
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%,.2f", tx?.amount ?: invoice.paidAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EduSuccess
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Cumulative Paid", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.paidAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Remaining Due Balance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%,.2f", invoice.dueAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (invoice.dueAmount > 0.0) EduError else EduSuccess
                            )
                        }
                    }
                }

                if (!tx?.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(EduSpacing.sm))
                    Text(
                        text = "Notes: ${tx?.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Close Receipt",
                onClick = onDismiss,
                testTag = "dismiss_receipt_button"
            )
        }
    )
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

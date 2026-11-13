package com.example.features.library.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.library.domain.BookCategory
import com.example.features.library.domain.LibraryBook
import com.example.features.library.domain.LoanStatus

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        isbn: String,
        title: String,
        author: String,
        publisher: String,
        category: BookCategory,
        shelfLocation: String,
        totalCopies: Int,
        publicationYear: Int,
        description: String
    ) -> Unit
) {
    var isbn by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BookCategory.TEXTBOOK) }
    var shelfLocation by remember { mutableStateOf("Wing A • Rack A-1") }
    var totalCopiesStr by remember { mutableStateOf("5") }
    var yearStr by remember { mutableStateOf("2025") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_add_book"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Catalog New Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title *") },
                    placeholder = { Text("e.g. Organic Chemistry Principles") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_book_title")
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author(s) *") },
                    placeholder = { Text("e.g. Paula Yurkanis Bruice") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_book_author")
                )

                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN-13 / Catalog Code *") },
                    placeholder = { Text("978-0-13-404228-2") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_book_isbn")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BookCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_add_cat_${category.name.lowercase()}")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = publisher,
                        onValueChange = { publisher = it },
                        label = { Text("Publisher") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("input_book_publisher")
                    )
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f).testTag("input_book_year")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shelfLocation,
                        onValueChange = { shelfLocation = it },
                        label = { Text("Shelf Location") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f).testTag("input_book_shelf")
                    )
                    OutlinedTextField(
                        value = totalCopiesStr,
                        onValueChange = { totalCopiesStr = it },
                        label = { Text("Copies") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f).testTag("input_book_copies")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Synopsis / Reference Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_book_description")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val copies = totalCopiesStr.toIntOrNull() ?: 1
                    val year = yearStr.toIntOrNull() ?: 2025
                    onConfirm(
                        isbn,
                        title,
                        author,
                        publisher,
                        selectedCategory,
                        shelfLocation,
                        copies,
                        year,
                        description
                    )
                },
                enabled = title.isNotBlank() && author.isNotBlank() && isbn.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_add_book")
            ) {
                Text("Add to Catalog")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_book")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun IssueBookDialog(
    book: LibraryBook,
    onDismiss: () -> Unit,
    onConfirm: (
        borrowerName: String,
        rollOrStaffId: String,
        borrowerRole: String,
        dueDate: String,
        notes: String
    ) -> Unit
) {
    var borrowerName by remember { mutableStateOf("") }
    var rollOrStaffId by remember { mutableStateOf("") }
    var borrowerRole by remember { mutableStateOf("Student") }
    var dueDate by remember { mutableStateOf("2026-04-10") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_issue_book"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Check Out Copy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = book.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${book.author} • ${book.availableCopies}/${book.totalCopies} available (${book.shelfLocation})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Student", "Faculty").forEach { role ->
                        FilterChip(
                            selected = borrowerRole == role,
                            onClick = { borrowerRole = role },
                            label = { Text(role) },
                            modifier = Modifier.testTag("chip_role_${role.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = borrowerName,
                    onValueChange = { borrowerName = it },
                    label = { Text("Borrower Full Name *") },
                    placeholder = { Text("e.g. Amina Rahman") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_issue_borrower_name")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollOrStaffId,
                        onValueChange = { rollOrStaffId = it },
                        label = { Text("Roll / Staff ID *") },
                        placeholder = { Text("OXF-10A-001") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_issue_borrower_id")
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_issue_due_date")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Circulation Notes") },
                    placeholder = { Text("Coursework / reading assignment notes...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_issue_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(borrowerName, rollOrStaffId, borrowerRole, dueDate, notes) },
                enabled = borrowerName.isNotBlank() && rollOrStaffId.isNotBlank() && dueDate.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_issue_book")
            ) {
                Text("Issue Copy")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_issue_book")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookDetailDialog(
    book: LibraryBook,
    onDismiss: () -> Unit,
    onIssueClick: () -> Unit,
    onReturnClick: (loanId: String, fineAmount: Double) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_book_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = book.category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "By ${book.author} • ${book.publisher.ifBlank { "Academic Press" }} (${book.publicationYear})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ISBN: ${book.isbn}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Shelf: ${book.shelfLocation}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Inventory: ${book.availableCopies} available / ${book.totalCopies} total copies (${book.issuedCopiesCount} checked out)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (book.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        if (book.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = book.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Circulation & Lending Ledger (${book.loans.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (book.loans.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No borrowing history recorded for this title.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    book.loans.forEach { loan ->
                        val isActive = loan.status == LoanStatus.ISSUED || loan.status == LoanStatus.OVERDUE
                        val badgeColor = when (loan.status) {
                            LoanStatus.ISSUED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                            LoanStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                            LoanStatus.RETURNED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                            LoanStatus.LOST -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${loan.borrowerName} (${loan.borrowerRole})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "ID: ${loan.rollOrStaffId} • Issued: ${loan.issueDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeColor.first)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = loan.status.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = badgeColor.second
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (loan.returnDate.isNotBlank()) "Returned: ${loan.returnDate}" else "Due: ${loan.dueDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (loan.status == LoanStatus.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (loan.fineAmount > 0.0) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Fine: $${"%.2f".format(loan.fineAmount)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    if (isActive && TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
                                        OutlinedButton(
                                            onClick = { onReturnClick(loan.loanId, loan.fineAmount) },
                                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_return_${loan.loanId}")
                                        ) {
                                            Text("Check In", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (book.isAvailable && TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
                Button(
                    onClick = onIssueClick,
                    modifier = Modifier.testTag("btn_issue_from_detail")
                ) {
                    Text("Issue Copy")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

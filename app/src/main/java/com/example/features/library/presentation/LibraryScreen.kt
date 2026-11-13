package com.example.features.library.presentation

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
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.features.library.domain.BookCategory
import com.example.features.library.domain.LibraryBook
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
        modifier = modifier.fillMaxSize().testTag("library_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Library & Circulation",
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
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_library_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
                        IconButton(
                            onClick = { viewModel.openAddBookDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_add_book")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Catalog Book")
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
            // Metrics Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EduStatCard(
                        title = "Catalog Titles",
                        value = "${uiState.totalTitlesCount}",
                        icon = Icons.Default.LocalLibrary,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "Available Copies",
                        value = "${uiState.availableCopiesCount}/${uiState.totalPhysicalCopies}",
                        icon = Icons.Default.Inventory2,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.width(170.dp)
                    )
                    EduStatCard(
                        title = "Checked Out",
                        value = "${uiState.activeIssuedCount}",
                        icon = Icons.Default.MenuBook,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "Overdue Loans",
                        value = "${uiState.overdueLoansCount}",
                        icon = Icons.Default.Warning,
                        iconTint = if (uiState.overdueLoansCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        iconContainerColor = if (uiState.overdueLoansCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search title, author, ISBN, shelf, or borrower...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_library")
                )
            }

            // Category & Availability Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.onlyAvailableFilter,
                        onClick = { viewModel.toggleOnlyAvailableFilter() },
                        leadingIcon = {
                            if (uiState.onlyAvailableFilter) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        label = { Text("Available Only") },
                        modifier = Modifier.testTag("chip_available_only")
                    )
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All Categories") },
                        modifier = Modifier.testTag("chip_cat_all")
                    )
                    BookCategory.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == category,
                            onClick = {
                                if (uiState.selectedCategoryFilter == category) {
                                    viewModel.setCategoryFilter(null)
                                } else {
                                    viewModel.setCategoryFilter(category)
                                }
                            },
                            label = { Text(category.displayName) },
                            modifier = Modifier.testTag("chip_cat_${category.name.lowercase()}")
                        )
                    }
                }
            }

            // Loading indicator
            item {
                AnimatedVisibility(visible = uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty State
            if (!uiState.isLoading && uiState.filteredBooks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching books in catalog",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Adjust search criteria or catalog new academic books for this school.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Catalog List
            items(uiState.filteredBooks, key = { it.id }) { book ->
                LibraryBookCard(
                    book = book,
                    onInspect = { viewModel.openBookDetail(book) },
                    onIssue = { viewModel.openIssueBookDialog(book) },
                    onDelete = { viewModel.deleteBook(book.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showAddBookDialog) {
        AddBookDialog(
            onDismiss = { viewModel.closeAddBookDialog() },
            onConfirm = { isbn, title, author, publisher, category, shelf, copies, year, desc ->
                viewModel.addBook(isbn, title, author, publisher, category, shelf, copies, year, desc)
            }
        )
    }

    if (uiState.showIssueBookDialog && uiState.selectedBookForAction != null) {
        IssueBookDialog(
            book = uiState.selectedBookForAction!!,
            onDismiss = { viewModel.closeIssueBookDialog() },
            onConfirm = { borrowerName, rollOrStaffId, borrowerRole, dueDate, notes ->
                viewModel.issueBook(
                    bookId = uiState.selectedBookForAction!!.id,
                    borrowerName = borrowerName,
                    rollOrStaffId = rollOrStaffId,
                    borrowerRole = borrowerRole,
                    dueDate = dueDate,
                    notes = notes
                )
            }
        )
    }

    if (uiState.showBookDetailDialog && uiState.selectedBookForAction != null) {
        val currentBook = uiState.selectedBookForAction!!
        BookDetailDialog(
            book = currentBook,
            onDismiss = { viewModel.closeBookDetail() },
            onIssueClick = {
                viewModel.closeBookDetail()
                viewModel.openIssueBookDialog(currentBook)
            },
            onReturnClick = { loanId, fineAmount ->
                viewModel.returnBook(currentBook.id, loanId, fineAmount)
            }
        )
    }
}

@Composable
fun LibraryBookCard(
    book: LibraryBook,
    onInspect: () -> Unit,
    onIssue: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("book_card_${book.id}")
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = book.category.displayName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ISBN: ${book.isbn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                val availBg = if (book.isAvailable) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                val availFg = if (book.isAvailable) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(availBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${book.availableCopies}/${book.totalCopies} Available",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = availFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = book.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "By ${book.author} • ${book.publisher.ifBlank { "Academic Edition" }} (${book.publicationYear})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = book.shelfLocation,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (book.overdueLoansCount > 0) {
                    Text(
                        text = "${book.overdueLoansCount} Overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "${book.activeLoansCount} Active Loan(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val availabilityFraction = if (book.totalCopies > 0) {
                book.availableCopies.toFloat() / book.totalCopies.toFloat()
            } else 0f
            LinearProgressIndicator(
                progress = { availabilityFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.testTag("btn_inspect_${book.id}")
                ) {
                    Text("Ledger (${book.loans.size})", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (TenantContext.hasPermission(Permission.LIBRARY_MANAGE)) {
                        OutlinedButton(
                            onClick = onIssue,
                            enabled = book.isAvailable,
                            modifier = Modifier.testTag("btn_issue_${book.id}")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Issue", fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_delete_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Book",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

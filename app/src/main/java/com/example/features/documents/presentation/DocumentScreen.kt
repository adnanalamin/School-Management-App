package com.example.features.documents.presentation

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.features.documents.domain.DocumentCategory
import com.example.features.documents.domain.DocumentVerificationStatus
import com.example.features.documents.domain.SchoolDocument
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel,
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
        modifier = modifier.fillMaxSize().testTag("documents_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Documents & Credentials",
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
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_documents_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
                        IconButton(
                            onClick = { viewModel.openCreateDocumentDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_add_document")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Publish Document")
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
            // Vault Summary Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EduStatCard(
                        title = "Total Records",
                        value = "${uiState.totalDocumentsCount}",
                        icon = Icons.Default.FolderShared,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(155.dp)
                    )
                    EduStatCard(
                        title = "Verified Vault",
                        value = "${uiState.verifiedDocumentsCount}",
                        icon = Icons.Default.Verified,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.width(155.dp)
                    )
                    EduStatCard(
                        title = "IDs & Certs",
                        value = "${uiState.credentialsCount}",
                        icon = Icons.Default.Badge,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.width(155.dp)
                    )
                    EduStatCard(
                        title = "Downloads",
                        value = "${uiState.totalDownloadsCount}",
                        icon = Icons.Default.Download,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.width(155.dp)
                    )
                }
            }

            // Search Input
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search title, ref code, recipient, grade, or file...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_documents")
                )
            }

            // Pinned & Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.onlyPinnedFilter,
                        onClick = { viewModel.toggleOnlyPinnedFilter() },
                        leadingIcon = {
                            if (uiState.onlyPinnedFilter) {
                                Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        label = { Text("Pinned") },
                        modifier = Modifier.testTag("chip_doc_pinned")
                    )
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All Types") },
                        modifier = Modifier.testTag("chip_doc_cat_all")
                    )
                    DocumentCategory.entries.forEach { category ->
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
                            modifier = Modifier.testTag("chip_doc_cat_${category.name.lowercase()}")
                        )
                    }
                }
            }

            // Verification Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All Statuses") },
                        modifier = Modifier.testTag("chip_doc_status_all")
                    )
                    DocumentVerificationStatus.entries.forEach { status ->
                        FilterChip(
                            selected = uiState.selectedStatusFilter == status,
                            onClick = {
                                if (uiState.selectedStatusFilter == status) {
                                    viewModel.setStatusFilter(null)
                                } else {
                                    viewModel.setStatusFilter(status)
                                }
                            },
                            label = { Text(status.displayName) },
                            modifier = Modifier.testTag("chip_doc_status_${status.name.lowercase()}")
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
            if (!uiState.isLoading && uiState.filteredDocuments.isEmpty()) {
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
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching documents found",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Adjust search criteria or publish a new syllabus, certificate, or handbook.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Document List
            items(uiState.filteredDocuments, key = { it.id }) { doc ->
                SchoolDocumentCard(
                    document = doc,
                    onInspect = { viewModel.openDocumentDetail(doc) },
                    onDownload = { viewModel.recordDownload(doc.id) },
                    onDelete = { viewModel.deleteDocument(doc.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showCreateDocumentDialog) {
        CreateDocumentDialog(
            onDismiss = { viewModel.closeCreateDocumentDialog() },
            onConfirm = { code, title, desc, cat, target, recipient, recipientId, fileName, fileSize, pinned ->
                viewModel.publishDocument(code, title, desc, cat, target, recipient, recipientId, fileName, fileSize, pinned)
            }
        )
    }

    if (uiState.showDocumentDetailDialog && uiState.selectedDocumentForAction != null) {
        val currentDoc = uiState.selectedDocumentForAction!!
        DocumentDetailDialog(
            document = currentDoc,
            onDismiss = { viewModel.closeDocumentDetail() },
            onDownloadClick = { viewModel.recordDownload(currentDoc.id) },
            onTogglePinClick = { viewModel.togglePin(currentDoc.id) },
            onStatusChange = { newStatus -> viewModel.updateVerificationStatus(currentDoc.id, newStatus) }
        )
    }
}

@Composable
fun SchoolDocumentCard(
    document: SchoolDocument,
    onInspect: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColors = when (document.verificationStatus) {
        DocumentVerificationStatus.VERIFIED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        DocumentVerificationStatus.PENDING_REVIEW -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        DocumentVerificationStatus.ARCHIVED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DocumentVerificationStatus.REVOKED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("doc_card_${document.id}")
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
                            imageVector = if (document.isOfficialCredential) Icons.Default.Badge else Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = document.category.displayName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (document.isPinned) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "${document.documentCode} • ${document.issuedDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColors.first)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = document.verificationStatus.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColors.second
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = document.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = document.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (document.issuedToName.isNotBlank()) {
                        "Recipient: ${document.issuedToName} (${document.issuedToId})"
                    } else {
                        "Audience: ${document.targetGradeOrDepartment}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${document.fileSizeLabel} • ${document.downloadCount} DL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

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
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_inspect_${document.id}")
                ) {
                    Text("Inspect", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onDownload,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_download_${document.id}")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 12.sp)
                    }
                    if (TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_delete_${document.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Document",
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

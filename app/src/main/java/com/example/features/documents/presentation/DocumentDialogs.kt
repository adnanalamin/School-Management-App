package com.example.features.documents.presentation

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Switch
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
import com.example.features.documents.domain.DocumentCategory
import com.example.features.documents.domain.DocumentVerificationStatus
import com.example.features.documents.domain.SchoolDocument

@Composable
fun CreateDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        documentCode: String,
        title: String,
        description: String,
        category: DocumentCategory,
        targetGradeOrDepartment: String,
        issuedToName: String,
        issuedToId: String,
        fileName: String,
        fileSizeLabel: String,
        isPinned: Boolean
    ) -> Unit
) {
    var documentCode by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.SYLLABUS) }
    var targetGradeOrDept by remember { mutableStateOf("Grade 10 • All Sections") }
    var issuedToName by remember { mutableStateOf("") }
    var issuedToId by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var fileSizeLabel by remember { mutableStateOf("1.5 MB") }
    var isPinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_create_document"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderShared,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publish Document / Credential",
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
                    label = { Text("Document Title *") },
                    placeholder = { Text("e.g. Grade 10 Organic Chemistry Syllabus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_doc_title")
                )

                Text(
                    text = "Document Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DocumentCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_add_doc_cat_${category.name.lowercase()}")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = documentCode,
                        onValueChange = { documentCode = it },
                        label = { Text("Ref Code") },
                        placeholder = { Text("SYL-OXF-26") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f).testTag("input_doc_code")
                    )
                    OutlinedTextField(
                        value = targetGradeOrDept,
                        onValueChange = { targetGradeOrDept = it },
                        label = { Text("Target Grade / Dept") },
                        singleLine = true,
                        modifier = Modifier.weight(1.1f).testTag("input_doc_target")
                    )
                }

                if (selectedCategory == DocumentCategory.CERTIFICATE || selectedCategory == DocumentCategory.ID_CARD) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = issuedToName,
                            onValueChange = { issuedToName = it },
                            label = { Text("Recipient Name") },
                            placeholder = { Text("Amina Rahman") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("input_doc_recipient")
                        )
                        OutlinedTextField(
                            value = issuedToId,
                            onValueChange = { issuedToId = it },
                            label = { Text("Roll / Staff ID") },
                            placeholder = { Text("OXF-10A-001") },
                            singleLine = true,
                            modifier = Modifier.weight(0.8f).testTag("input_doc_recipient_id")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("Attachment File *") },
                        placeholder = { Text("Chemistry_2026.pdf") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f).testTag("input_doc_filename")
                    )
                    OutlinedTextField(
                        value = fileSizeLabel,
                        onValueChange = { fileSizeLabel = it },
                        label = { Text("Size") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f).testTag("input_doc_filesize")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin to Top of Vault", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        modifier = Modifier.testTag("switch_doc_pin")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Summary & Verification Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_doc_description")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        documentCode,
                        title,
                        description,
                        selectedCategory,
                        targetGradeOrDept,
                        issuedToName,
                        issuedToId,
                        fileName,
                        fileSizeLabel,
                        isPinned
                    )
                },
                enabled = title.isNotBlank() && fileName.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_create_doc")
            ) {
                Text("Publish Record")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_create_doc")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DocumentDetailDialog(
    document: SchoolDocument,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onTogglePinClick: () -> Unit,
    onStatusChange: (DocumentVerificationStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_document_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (document.isOfficialCredential) Icons.Default.Badge else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = document.category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
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
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (document.verificationStatus == DocumentVerificationStatus.VERIFIED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Digitally Verified • ${document.documentCode}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

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
                            text = "File: ${document.fileName} (${document.fileSizeLabel})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target: ${document.targetGradeOrDepartment} • ${document.academicYear}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (document.issuedToName.isNotBlank()) {
                            Text(
                                text = "Issued To: ${document.issuedToName} (${document.issuedToId})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Issued By: ${document.issuedBy} on ${document.issuedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Verified Downloads: ${document.downloadCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (document.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = document.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (TenantContext.hasPermission(Permission.DOCUMENTS_MANAGE)) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verification Status",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = onTogglePinClick,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_toggle_pin_doc")
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (document.isPinned) "Unpin" else "Pin", fontSize = 11.sp)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DocumentVerificationStatus.entries.forEach { statusOption ->
                            FilterChip(
                                selected = document.verificationStatus == statusOption,
                                onClick = { onStatusChange(statusOption) },
                                label = { Text(statusOption.displayName, fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_detail_doc_status_${statusOption.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDownloadClick,
                modifier = Modifier.testTag("btn_download_from_detail")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

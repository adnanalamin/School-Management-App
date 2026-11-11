package com.example.features.notices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.notices.domain.NoticeCategory
import com.example.features.notices.domain.NoticePriority
import com.example.features.notices.domain.NoticeTargetAudience
import com.example.features.notices.domain.SchoolNotice
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoticeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        content: String,
        category: NoticeCategory,
        priority: NoticePriority,
        targetAudience: NoticeTargetAudience,
        isPinned: Boolean,
        attachmentTitle: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NoticeCategory.ACADEMIC) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(NoticePriority.NORMAL) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var selectedAudience by remember { mutableStateOf(NoticeTargetAudience.ALL) }
    var audienceExpanded by remember { mutableStateOf(false) }
    var isPinned by remember { mutableStateOf(false) }
    var attachmentTitle by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Broadcast School Notice",
                    style = MaterialTheme.typography.titleLarge,
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
                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EduErrorContainer),
                        shape = EduShapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EduError)
                            Text(text = validationError ?: "", color = EduError, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title *") },
                    placeholder = { Text("e.g. Schedule for Annual Science Fair 2026") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Announcement Details *") },
                    placeholder = { Text("Enter detailed circular directives and guidelines...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_content_input"),
                    minLines = 3,
                    maxLines = 6
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Thematic Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("notice_category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        NoticeCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority & Audience Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = priorityExpanded,
                        onExpandedChange = { priorityExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPriority.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Urgency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("notice_priority_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            NoticePriority.entries.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.displayName) },
                                    onClick = {
                                        selectedPriority = p
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = audienceExpanded,
                        onExpandedChange = { audienceExpanded = it },
                        modifier = Modifier.weight(1.1f)
                    ) {
                        OutlinedTextField(
                            value = selectedAudience.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audienceExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("notice_audience_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = audienceExpanded,
                            onDismissRequest = { audienceExpanded = false }
                        ) {
                            NoticeTargetAudience.entries.forEach { aud ->
                                DropdownMenuItem(
                                    text = { Text(aud.displayName) },
                                    onClick = {
                                        selectedAudience = aud
                                        audienceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = attachmentTitle,
                    onValueChange = { attachmentTitle = it },
                    label = { Text("Attached Document (Optional)") },
                    placeholder = { Text("e.g. Science_Fair_Guidelines.pdf") },
                    leadingIcon = { Icon(Icons.Default.Attachment, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_attachment_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        modifier = Modifier.testTag("notice_pin_checkbox")
                    )
                    Text("Pin this circular to top of Noticeboard", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Broadcast Notice",
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        validationError = "Title and circular body cannot be blank."
                        return@EduButton
                    }
                    onConfirm(
                        title.trim(),
                        content.trim(),
                        selectedCategory,
                        selectedPriority,
                        selectedAudience,
                        isPinned,
                        attachmentTitle.trim()
                    )
                },
                modifier = Modifier.testTag("submit_notice_button")
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_notice_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NoticeDetailDialog(
    notice: SchoolNotice,
    onDismiss: () -> Unit
) {
    val priorityColor = when (notice.priority) {
        NoticePriority.URGENT -> EduError
        NoticePriority.IMPORTANT -> Color(0xFFF57C00) // Amber
        NoticePriority.NORMAL -> MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${notice.category.displayName} • Published: ${notice.publishedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = notice.priority.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Audience & Author metadata banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(EduShapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Audience: ${notice.targetAudience.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = notice.authorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Notice Content Body
                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )

                // Attachment if present
                if (notice.attachmentTitle.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = EduShapes.small,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Attachment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Attached Circular Document", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(notice.attachmentTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Verification footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EduSuccess, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Officially Authorized Institutional Announcement",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EduSuccess
                    )
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.testTag("close_notice_detail_button")
            )
        }
    )
}

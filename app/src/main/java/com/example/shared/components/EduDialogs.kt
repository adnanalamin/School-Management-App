package com.example.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.EduError
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing

@Composable
fun EduConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    testTag: String = "edu_confirm_dialog"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDestructive) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = EduError,
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(EduSpacing.sm))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) EduError else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            EduButton(
                text = confirmText,
                onClick = onConfirm,
                variant = if (isDestructive) EduButtonVariant.DANGER else EduButtonVariant.PRIMARY,
                testTag = "${testTag}_confirm"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("${testTag}_cancel")
            ) {
                Text(text = cancelText)
            }
        },
        shape = EduShapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag(testTag)
    )
}

@Composable
fun EduPromptDialog(
    title: String,
    message: String,
    initialValue: String = "",
    label: String = "Input",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Submit",
    cancelText: String = "Cancel",
    testTag: String = "edu_prompt_dialog"
) {
    var textValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                EduTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = label,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "${testTag}_input"
                )
            }
        },
        confirmButton = {
            EduButton(
                text = confirmText,
                onClick = { onConfirm(textValue) },
                enabled = textValue.isNotBlank(),
                testTag = "${testTag}_confirm"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("${testTag}_cancel")
            ) {
                Text(text = cancelText)
            }
        },
        shape = EduShapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag(testTag)
    )
}

package com.example.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduFilterBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    testTag: String = "edu_filter_bottom_sheet",
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EduSpacing.lg)
                .padding(bottom = EduSpacing.xxl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("${testTag}_reset")
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(EduSpacing.md))

            content()

            Spacer(modifier = Modifier.height(EduSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
            ) {
                EduButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = EduButtonVariant.OUTLINED,
                    modifier = Modifier.weight(1f),
                    testTag = "${testTag}_cancel"
                )
                EduButton(
                    text = "Apply Filters",
                    onClick = onApply,
                    variant = EduButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    testTag = "${testTag}_apply"
                )
            }
        }
    }
}

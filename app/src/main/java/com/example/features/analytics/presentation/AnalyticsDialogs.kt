package com.example.features.analytics.presentation

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.analytics.domain.AnalyticsDomain
import com.example.features.analytics.domain.ExecutiveReportSnapshot
import com.example.features.analytics.domain.ReportPeriod

@Composable
fun GenerateReportDialog(
    initialDomain: AnalyticsDomain,
    initialPeriod: ReportPeriod,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        domain: AnalyticsDomain,
        period: ReportPeriod,
        exportFormat: String,
        summaryNotes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf(initialDomain) }
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }
    var selectedFormat by remember { mutableStateOf("CSV") }
    var summaryNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_generate_report"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Executive Report",
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
                    label = { Text("Report Title *") },
                    placeholder = { Text("e.g. Q2 Governance & Tuition Rollup") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_report_title")
                )

                Text(
                    text = "Analytics Domain",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnalyticsDomain.entries.forEach { domain ->
                        FilterChip(
                            selected = selectedDomain == domain,
                            onClick = { selectedDomain = domain },
                            label = { Text(domain.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_gen_domain_${domain.name.lowercase()}")
                        )
                    }
                }

                Text(
                    text = "Reporting Period",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReportPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_gen_period_${period.name.lowercase()}")
                        )
                    }
                }

                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CSV", "PDF").forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_gen_fmt_${fmt.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = summaryNotes,
                    onValueChange = { summaryNotes = it },
                    label = { Text("Executive Commentary") },
                    placeholder = { Text("Highlights for school board or department heads...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_report_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, selectedDomain, selectedPeriod, selectedFormat, summaryNotes) },
                enabled = title.isNotBlank(),
                modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_confirm_generate_report")
            ) {
                Text("Generate Snapshot")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_cancel_generate_report")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportPreviewDialog(
    report: ExecutiveReportSnapshot,
    csvPayload: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_report_preview"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${report.reportCode} • ${report.exportFormat}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
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
                    text = report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${report.domain.displayName} • ${report.period.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Generated by ${report.generatedBy} on ${report.generatedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = report.headlineKpi,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (report.summaryNotes.isNotBlank()) {
                    Text(
                        text = report.summaryNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Text(
                    text = "Structured CSV Data Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = csvPayload,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(10.dp).testTag("txt_csv_preview")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_close_report_preview")
            ) {
                Text("Done")
            }
        }
    )
}

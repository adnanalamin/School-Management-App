package com.example.features.classes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.features.classes.domain.AcademicClass
import com.example.features.classes.domain.ClassSection
import com.example.features.classes.domain.SubjectAllocation
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

@Composable
fun CreateClassDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, gradeNum: Int, stream: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var gradeNumStr by remember { mutableStateOf("10") }
    var stream by remember { mutableStateOf("Science & Tech") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("create_class_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(EduSpacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Academic Class / Grade",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Class Name (e.g., Grade 10) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_class_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code (e.g., G10) *") },
                        modifier = Modifier.weight(1f).testTag("input_class_code"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gradeNumStr,
                        onValueChange = { gradeNumStr = it },
                        label = { Text("Numeric Level *") },
                        modifier = Modifier.weight(1f).testTag("input_class_grade_num"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = stream,
                    onValueChange = { stream = it },
                    label = { Text("Academic Stream (e.g. Science, Arts, General)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_class_stream"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(EduSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = EduButtonVariant.OUTLINED,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    EduButton(
                        text = "Create Class",
                        onClick = {
                            val gradeNum = gradeNumStr.toIntOrNull() ?: 10
                            onConfirm(name, code, gradeNum, stream)
                        },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                        testTag = "btn_submit_create_class"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSectionDialog(
    classes: List<AcademicClass>,
    preSelectedClassId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (classId: String, sectionName: String, roomNumber: String, capacity: Int, teacherId: String?, teacherName: String?) -> Unit
) {
    var selectedClassId by remember { mutableStateOf(preSelectedClassId ?: classes.firstOrNull()?.id ?: "") }
    var sectionName by remember { mutableStateOf("Section A") }
    var roomNumber by remember { mutableStateOf("Room 301") }
    var capacityStr by remember { mutableStateOf("35") }
    var teacherName by remember { mutableStateOf("Dr. Sarah Jenkins") }
    var teacherId by remember { mutableStateOf("tea_oxf_01") }

    var expandedClassDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("create_section_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(EduSpacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Class Section",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Class selection
                ExposedDropdownMenuBox(
                    expanded = expandedClassDropdown,
                    onExpandedChange = { expandedClassDropdown = !expandedClassDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentClassName = classes.firstOrNull { it.id == selectedClassId }?.name ?: "Select Class"
                    OutlinedTextField(
                        value = currentClassName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parent Class *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClassDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("select_parent_class")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedClassDropdown,
                        onDismissRequest = { expandedClassDropdown = false }
                    ) {
                        classes.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text("${cls.name} (${cls.code})") },
                                onClick = {
                                    selectedClassId = cls.id
                                    expandedClassDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    OutlinedTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        label = { Text("Section Name *") },
                        modifier = Modifier.weight(1f).testTag("input_section_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = capacityStr,
                        onValueChange = { capacityStr = it },
                        label = { Text("Capacity *") },
                        modifier = Modifier.weight(1f).testTag("input_section_capacity"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Assigned Room Number *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_section_room"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Appointed Class Teacher Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_section_teacher"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(EduSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = EduButtonVariant.OUTLINED,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    EduButton(
                        text = "Create Section",
                        onClick = {
                            val cap = capacityStr.toIntOrNull() ?: 35
                            onConfirm(selectedClassId, sectionName, roomNumber, cap, teacherId, teacherName)
                        },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                        testTag = "btn_submit_create_section"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllocateSubjectDialog(
    sections: List<ClassSection>,
    onDismiss: () -> Unit,
    onConfirm: (sectionId: String, subjectName: String, subjectCode: String, teacherId: String, teacherName: String, periodsPerWeek: Int, isElective: Boolean, roomNumber: String) -> Unit
) {
    var selectedSectionId by remember { mutableStateOf(sections.firstOrNull()?.id ?: "") }
    var subjectName by remember { mutableStateOf("") }
    var subjectCode by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("Prof. Alan Turing") }
    var teacherId by remember { mutableStateOf("tea_oxf_02") }
    var periodsPerWeek by remember { mutableIntStateOf(5) }
    var isElective by remember { mutableStateOf(false) }
    var roomNumber by remember { mutableStateOf("") }

    var expandedSectionDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("allocate_subject_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(EduSpacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allocate Subject & Faculty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Section picker
                ExposedDropdownMenuBox(
                    expanded = expandedSectionDropdown,
                    onExpandedChange = { expandedSectionDropdown = !expandedSectionDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentSec = sections.firstOrNull { it.id == selectedSectionId }?.displayName ?: "Select Section"
                    OutlinedTextField(
                        value = currentSec,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Class Section *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSectionDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("select_allocation_section")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSectionDropdown,
                        onDismissRequest = { expandedSectionDropdown = false }
                    ) {
                        sections.forEach { sec ->
                            DropdownMenuItem(
                                text = { Text(sec.displayName) },
                                onClick = {
                                    selectedSectionId = sec.id
                                    expandedSectionDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject (e.g. Physics) *") },
                        modifier = Modifier.weight(1.3f).testTag("input_alloc_subject_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Code *") },
                        modifier = Modifier.weight(0.7f).testTag("input_alloc_subject_code"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Instructing Faculty Member *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_alloc_teacher_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    OutlinedTextField(
                        value = periodsPerWeek.toString(),
                        onValueChange = { periodsPerWeek = it.toIntOrNull() ?: 4 },
                        label = { Text("Periods / Week") },
                        modifier = Modifier.weight(1f).testTag("input_alloc_periods"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Lab / Room") },
                        modifier = Modifier.weight(1f).testTag("input_alloc_room"),
                        singleLine = true
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Elective Course", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(text = "Mark as optional/elective subject for students", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isElective,
                            onCheckedChange = { isElective = it },
                            modifier = Modifier.testTag("switch_alloc_elective")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(EduSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = EduButtonVariant.OUTLINED,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    EduButton(
                        text = "Allocate Subject",
                        onClick = {
                            onConfirm(selectedSectionId, subjectName, subjectCode, teacherId, teacherName, periodsPerWeek, isElective, roomNumber)
                        },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                        testTag = "btn_submit_allocate_subject"
                    )
                }
            }
        }
    }
}

@Composable
fun SectionInspectionDialog(
    section: ClassSection,
    allocations: List<SubjectAllocation>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(600.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("section_inspection_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EduSpacing.lg, vertical = EduSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = section.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${section.roomNumber} • Academic Session ${section.academicYear}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(EduSpacing.lg)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    // Capacity status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Room Capacity Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${section.currentEnrollment} / ${section.capacity} Students",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { section.occupancyPercentage / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (section.occupancyPercentage > 90f) EduWarning else EduSuccess,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    // Class Teacher Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Appointed Class In-Charge", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = section.classTeacherName ?: "Not Assigned",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Subject Routine List
                    Text(
                        text = "Subject Timetable Allocations (${allocations.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (allocations.isEmpty()) {
                        Text(
                            text = "No subjects currently allocated to this section.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        allocations.forEach { alloc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = alloc.subjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            EduBadge(text = alloc.subjectCode, type = BadgeType.PRIMARY)
                                            if (alloc.isElective) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                EduBadge(text = "Elective", type = BadgeType.WARNING)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${alloc.teacherName} • ${alloc.roomNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    EduBadge(text = "${alloc.periodsPerWeek} periods/wk", type = BadgeType.INFO)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(EduSpacing.md),
                    horizontalArrangement = Arrangement.End
                ) {
                    EduButton(
                        text = "Close",
                        onClick = onDismiss,
                        variant = EduButtonVariant.PRIMARY
                    )
                }
            }
        }
    }
}

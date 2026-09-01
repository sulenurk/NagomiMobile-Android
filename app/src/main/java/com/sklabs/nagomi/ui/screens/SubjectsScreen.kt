package com.sklabs.nagomi.ui.screens

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sklabs.nagomi.data.local.SubjectEntity
import com.sklabs.nagomi.data.repository.NagomiRepository
import com.sklabs.nagomi.ui.subjects.SubjectsViewModel
import com.sklabs.nagomi.ui.localization.LocalNagomiStrings

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel = viewModel(),
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val selectedColor by viewModel.selectedColor.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var subjectName by rememberSaveable { mutableStateOf("") }
    val strings = LocalNagomiStrings.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(
            strings.text("manage_subjects", "Manage Subjects"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            strings.text("subjects_description", "Create categories for your tasks and statistics."),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text(strings.text("new_subject", "Subject name")) },
                    placeholder = { Text(strings.text("subject_name_example", "e.g. Mathematics")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text(strings.text("select_color", "Select color"), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                NagomiRepository.SUBJECT_COLORS.chunked(4).forEach { colorRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        colorRow.forEach { hex ->
                            ColorDot(
                                hex = hex,
                                selected = hex == selectedColor,
                                onClick = { viewModel.selectColor(hex) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.addSubject(subjectName) { subjectName = "" }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" ${strings.text("add_subject", "Add Subject")}")
                }
                if (status.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(strings.status(status), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(strings.text("subjects", "Subjects"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(subjects, key = { it.id }) { subject ->
                SubjectCard(
                    subject = subject,
                    onCycleColor = { viewModel.cycleColor(subject.id, subject.color) },
                    onDelete = { viewModel.deleteSubject(subject.id) },
                )
            }
        }
    }
}

@Composable
private fun ColorDot(hex: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape,
            )
            .padding(4.dp)
            .background(hex.toComposeColor(), CircleShape)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SubjectCard(
    subject: SubjectEntity,
    onCycleColor: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalNagomiStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(18.dp).background(subject.color.toComposeColor(), CircleShape))
            Text(
                text = if (subject.isDefault) strings.text("other_subject", "Other") else subject.name.orEmpty(),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onCycleColor) {
                Icon(Icons.Default.Palette, contentDescription = strings.text("change_color", "Change color"))
            }
            if (!subject.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = strings.text("delete_subject", "Delete subject"),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

internal fun String.toComposeColor(): Color = runCatching { Color(parseColor(this)) }
    .getOrDefault(Color(0xFFA78BFA))

package com.sklabs.nagomi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sklabs.nagomi.data.local.SubjectEntity
import com.sklabs.nagomi.data.local.TaskWithSubject
import com.sklabs.nagomi.data.repository.NagomiRepository
import com.sklabs.nagomi.ui.studyplan.StudyPlanViewModel
import com.sklabs.nagomi.ui.studyplan.TaskFilter
import com.sklabs.nagomi.ui.localization.LocalNagomiStrings

@Composable
fun StudyPlanScreen(
    onStartFocus: () -> Unit,
    viewModel: StudyPlanViewModel = viewModel(),
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val tasks by viewModel.visibleTasks.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    var showClearPlanDialog by rememberSaveable { mutableStateOf(false) }
    val strings = LocalNagomiStrings.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text(strings.text("daily_study_plan", "Daily Study Plan"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                strings.text("study_plan_description", "Plan your tasks, then run the full queue in Focus Timer."),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            TaskFormCard(
                subjects = subjects,
                editingTask = editingTask,
                status = status,
                onSave = viewModel::saveTask,
                onCancelEdit = viewModel::cancelEdit,
            )
        }

        item {
            Button(
                onClick = { viewModel.startPlan(onStartFocus) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(" ${strings.text("start_plan", "Start Plan")}")
            }
        }

        item {
            Column {
                Text(strings.text("tasks", "Tasks"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                TaskFilter.entries.chunked(2).forEach { rowFilters ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowFilters.forEach { option ->
                            FilterChip(
                                selected = filter == option,
                                onClick = { viewModel.setFilter(option) },
                                label = {
                                    Text(
                                        when (option) {
                                            TaskFilter.ALL -> strings.text("all", option.label)
                                            TaskFilter.PENDING -> strings.text("pending", option.label)
                                            TaskFilter.ACTIVE -> strings.text("active", option.label)
                                            TaskFilter.COMPLETED -> strings.text("completed", option.label)
                                        },
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Text(
                    strings.text("no_tasks_in_view", "No tasks in this view."),
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(tasks, key = { it.task.id }) { task ->
            TaskCard(
                item = task,
                onToggleComplete = { viewModel.toggleComplete(task.task.id) },
                onStart = { viewModel.startTask(task.task.id, onStartFocus) },
                onEdit = { viewModel.beginEdit(task) },
                onDuplicate = { viewModel.duplicateTask(task.task.id) },
                onDelete = { viewModel.deleteTask(task.task.id) },
                onMoveUp = { viewModel.moveTask(task, -1) },
                onMoveDown = { viewModel.moveTask(task, 1) },
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::clearCompletedTasks,
                    modifier = Modifier.weight(1f),
                ) { Text(strings.text("clear_completed", "Clear completed")) }
                OutlinedButton(
                    onClick = { showClearPlanDialog = true },
                    modifier = Modifier.weight(1f),
                ) { Text(strings.text("clear_study_plan", "Clear plan"), color = MaterialTheme.colorScheme.error) }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    if (showClearPlanDialog) {
        AlertDialog(
            onDismissRequest = { showClearPlanDialog = false },
            title = { Text(strings.text("clear_study_plan", "Clear Study Plan")) },
            text = { Text(strings.text("clear_study_plan_confirmation", "All tasks will be removed. Your statistics will not be affected.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearPlanDialog = false
                        viewModel.clearStudyPlan()
                    },
                ) { Text(strings.text("clear", "Clear"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearPlanDialog = false }) { Text(strings.text("cancel", "Cancel")) }
            },
        )
    }
}

@Composable
private fun TaskFormCard(
    subjects: List<SubjectEntity>,
    editingTask: TaskWithSubject?,
    status: String,
    onSave: (String, String, String, String, String, () -> Unit) -> Unit,
    onCancelEdit: () -> Unit,
) {
    val strings = LocalNagomiStrings.current
    var title by rememberSaveable { mutableStateOf("") }
    var subjectId by rememberSaveable { mutableStateOf(NagomiRepository.OTHER_SUBJECT_ID) }
    var focusMinutes by rememberSaveable { mutableStateOf("25") }
    var breakMinutes by rememberSaveable { mutableStateOf("5") }
    var priority by rememberSaveable { mutableStateOf("medium") }

    fun clearForm() {
        title = ""
        subjectId = NagomiRepository.OTHER_SUBJECT_ID
        focusMinutes = "25"
        breakMinutes = "5"
        priority = "medium"
    }

    LaunchedEffect(editingTask?.task?.id) {
        editingTask?.task?.let { task ->
            title = task.title
            subjectId = task.subjectId
            focusMinutes = task.focusDurationMinutes.toString()
            breakMinutes = task.breakMinutes.toString()
            priority = task.priority
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (editingTask == null) strings.text("add_task", "Add Task") else strings.text("edit_task", "Edit Task"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (editingTask != null) {
                    IconButton(onClick = { onCancelEdit(); clearForm() }) {
                        Icon(Icons.Default.Close, contentDescription = strings.text("cancel", "Cancel edit"))
                    }
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(strings.text("task_name", "Task name")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = focusMinutes,
                    onValueChange = { focusMinutes = it.filter(Char::isDigit) },
                    label = { Text(strings.text("focus_duration_minutes", "Focus min")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = breakMinutes,
                    onValueChange = { breakMinutes = it.filter(Char::isDigit) },
                    label = { Text(strings.text("break_minutes", "Break min")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SelectionMenu(
                    label = strings.text("subject", "Subject"),
                    selected = subjectId,
                    options = subjects.map {
                        it.id to if (it.isDefault) strings.text("other_subject", "Other") else it.name.orEmpty()
                    },
                    onSelected = { subjectId = it },
                    modifier = Modifier.weight(1f),
                )
                SelectionMenu(
                    label = strings.text("priority", "Priority"),
                    selected = priority,
                    options = listOf(
                        "low" to strings.text("low", "Low"),
                        "medium" to strings.text("medium", "Medium"),
                        "high" to strings.text("high", "High"),
                    ),
                    onSelected = { priority = it },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onSave(title, subjectId, focusMinutes, breakMinutes, priority) { clearForm() }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(if (editingTask == null) Icons.Default.Add else Icons.Default.Edit, contentDescription = null)
                Text(if (editingTask == null) " ${strings.text("add_task", "Add Task")}" else " ${strings.text("save_changes", "Save Changes")}")
            }
            if (status.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(strings.status(status), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SelectionMenu(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: options.firstOrNull()?.second.orEmpty()
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(selectedLabel, maxLines = 1)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelected(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    item: TaskWithSubject,
    onToggleComplete: () -> Unit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val strings = LocalNagomiStrings.current
    val task = item.task
    val completed = task.status == "completed"
    val active = task.status == "active"
    val priorityColor = when (task.priority) {
        "high" -> Color(0xFFEF4444)
        "low" -> Color(0xFF22C55E)
        else -> Color(0xFFF59E0B)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.then(
            if (active) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            else Modifier,
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completed, onCheckedChange = { onToggleComplete() })
                Box(Modifier.size(12.dp).background((item.subjectColor ?: "#A78BFA").toComposeColor(), CircleShape))
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(task.title, fontWeight = FontWeight.Bold)
                    Text(
                        "${if (item.subjectIsDefault == true) strings.text("other_subject", "Other") else item.subjectName.orEmpty()} · " +
                            strings.format(
                                "task_detail",
                                "{focus} min focus · {break_minutes} min break",
                                "focus" to task.focusDurationMinutes,
                                "break_minutes" to task.breakMinutes,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    strings.text(task.priority, task.priority.replaceFirstChar { it.uppercase() }),
                    color = priorityColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onMoveUp) { Icon(Icons.Default.KeyboardArrowUp, strings.text("move_up", "Move up")) }
                IconButton(onClick = onMoveDown) { Icon(Icons.Default.KeyboardArrowDown, strings.text("move_down", "Move down")) }
                if (!completed) {
                    IconButton(onClick = onStart) { Icon(Icons.Default.PlayArrow, strings.text("start_task", "Start task")) }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, strings.text("edit_task", "Edit task")) }
                    IconButton(onClick = onDuplicate) { Icon(Icons.Default.ContentCopy, strings.text("duplicate_task", "Duplicate task")) }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, strings.text("delete_task", "Delete task"), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

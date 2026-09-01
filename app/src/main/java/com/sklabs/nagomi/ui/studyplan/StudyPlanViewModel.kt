package com.sklabs.nagomi.ui.studyplan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.local.TaskWithSubject
import com.sklabs.nagomi.data.repository.NagomiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
}

class StudyPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NagomiRepository(NagomiDatabase.getInstance(application))

    val subjects = repository.subjects.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    val tasks = repository.tasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter = _filter.asStateFlow()

    val visibleTasks = combine(tasks, filter) { allTasks, selectedFilter ->
        when (selectedFilter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> allTasks.filter { it.task.status == "pending" }
            TaskFilter.ACTIVE -> allTasks.filter { it.task.status == "active" }
            TaskFilter.COMPLETED -> allTasks.filter { it.task.status == "completed" }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _editingTask = MutableStateFlow<TaskWithSubject?>(null)
    val editingTask = _editingTask.asStateFlow()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    init {
        viewModelScope.launch { repository.ensureDefaultSubject() }
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun beginEdit(task: TaskWithSubject) {
        _editingTask.value = task
        _status.value = "Edit mode"
    }

    fun cancelEdit() {
        _editingTask.value = null
        _status.value = ""
    }

    fun saveTask(
        title: String,
        subjectId: String,
        focusMinutesText: String,
        breakMinutesText: String,
        priority: String,
        onSaved: () -> Unit,
    ) {
        val focusMinutes = focusMinutesText.toIntOrNull()
        val breakMinutes = breakMinutesText.toIntOrNull()
        if (focusMinutes == null || breakMinutes == null) {
            _status.value = "Durations must be numbers"
            return
        }
        if (focusMinutes <= 0 || breakMinutes < 0) {
            _status.value = "Focus must be above zero and break cannot be negative"
            return
        }

        val editingId = _editingTask.value?.task?.id
        viewModelScope.launch {
            repository.saveTask(
                editingId = editingId,
                title = title.trim().ifEmpty { "New task" },
                subjectId = subjectId.ifEmpty { NagomiRepository.OTHER_SUBJECT_ID },
                focusMinutes = focusMinutes,
                breakMinutes = breakMinutes,
                priority = priority,
            )
            _editingTask.value = null
            _status.value = if (editingId == null) "Task added" else "Task updated"
            onSaved()
        }
    }

    fun toggleComplete(id: String) {
        viewModelScope.launch { repository.toggleComplete(id) }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch { repository.deleteTask(id) }
    }

    fun duplicateTask(id: String) {
        viewModelScope.launch { repository.duplicateTask(id) }
    }

    fun moveTask(task: TaskWithSubject, direction: Int) {
        val visible = visibleTasks.value
        val index = visible.indexOfFirst { it.task.id == task.task.id }
        val targetIndex = index + direction
        if (index < 0 || targetIndex !in visible.indices) return

        viewModelScope.launch { repository.moveTask(task, visible[targetIndex]) }
    }

    fun startTask(id: String, onStarted: () -> Unit) {
        viewModelScope.launch {
            if (repository.startTask(id)) onStarted()
        }
    }

    fun startPlan(onStarted: () -> Unit) {
        viewModelScope.launch {
            if (repository.startPlan()) {
                _status.value = "Study plan started"
                onStarted()
            } else {
                _status.value = "No tasks to start"
            }
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            _status.value = if (repository.clearCompletedTasks()) {
                "Completed tasks cleared"
            } else {
                "No completed tasks"
            }
        }
    }

    fun clearStudyPlan() {
        viewModelScope.launch {
            repository.clearStudyPlan()
            _editingTask.value = null
            _status.value = "Study plan cleared"
        }
    }
}

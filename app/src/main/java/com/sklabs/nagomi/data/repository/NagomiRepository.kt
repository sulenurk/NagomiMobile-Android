package com.sklabs.nagomi.data.repository

import androidx.room.withTransaction
import com.sklabs.nagomi.data.local.FocusSessionEntity
import com.sklabs.nagomi.data.local.FocusTimerStateEntity
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.local.StudyTaskEntity
import com.sklabs.nagomi.data.local.SubjectEntity
import com.sklabs.nagomi.data.local.TaskWithSubject
import java.util.UUID

class NagomiRepository(
    private val database: NagomiDatabase,
) {
    val subjects = database.subjectDao().observeAll()
    val tasks = database.studyTaskDao().observeAllWithSubjects()
    val activeTask = database.studyTaskDao().observeActiveWithSubject()
    val focusQueue = database.studyTaskDao().observeQueueWithSubjects()
    val completedSessions = database.focusSessionDao().observeCompleted()

    suspend fun ensureDefaultSubject() {
        if (database.subjectDao().getById(OTHER_SUBJECT_ID) == null) {
            database.subjectDao().insert(
                SubjectEntity(
                    id = OTHER_SUBJECT_ID,
                    nameKey = "other_subject",
                    color = SUBJECT_COLORS.first(),
                    isDefault = true,
                    position = 0,
                ),
            )
        }
    }

    suspend fun addSubject(name: String, color: String) {
        val position = (database.subjectDao().maxPosition() ?: 0) + 1
        database.subjectDao().insert(
            SubjectEntity(
                id = "subject_${UUID.randomUUID().toString().take(8)}",
                name = name.trim(),
                color = color,
                position = position,
            ),
        )
    }

    suspend fun changeSubjectColor(id: String, color: String) {
        database.subjectDao().updateColor(id, color)
    }

    suspend fun deleteSubject(id: String) = database.withTransaction {
        val subject = database.subjectDao().getById(id) ?: return@withTransaction
        if (subject.isDefault) return@withTransaction

        database.studyTaskDao().reassignSubject(id)
        database.focusSessionDao().reassignSubject(id)
        database.subjectDao().deleteCustom(id)
    }

    suspend fun saveTask(
        editingId: String?,
        title: String,
        subjectId: String,
        focusMinutes: Int,
        breakMinutes: Int,
        priority: String,
    ) {
        val existing = editingId?.let { database.studyTaskDao().getById(it) }
        if (existing != null) {
            database.studyTaskDao().update(
                existing.copy(
                    title = title.trim(),
                    subjectId = subjectId,
                    focusDurationMinutes = focusMinutes,
                    breakMinutes = breakMinutes,
                    priority = priority,
                    status = "pending",
                    completedAtMillis = null,
                    hiddenFromPlan = false,
                    hiddenFromCompleted = false,
                ),
            )
        } else {
            val position = (database.studyTaskDao().maxPosition() ?: -1) + 1
            database.studyTaskDao().insert(
                StudyTaskEntity(
                    id = "task_${UUID.randomUUID().toString().take(8)}",
                    subjectId = subjectId,
                    title = title.trim(),
                    focusDurationMinutes = focusMinutes,
                    breakMinutes = breakMinutes,
                    priority = priority,
                    position = position,
                ),
            )
        }
    }

    suspend fun deleteTask(id: String) = database.withTransaction {
        if (database.studyTaskDao().getById(id)?.status == "active") {
            database.focusTimerStateDao().clear()
        }
        database.focusSessionDao().deleteStudyPlanSessions(id)
        database.studyTaskDao().deleteById(id)
    }

    suspend fun clearCompletedTasks(): Boolean = database.withTransaction {
        val hasCompleted = database.studyTaskDao().getAll().any { it.status == "completed" }
        if (!hasCompleted) return@withTransaction false
        database.studyTaskDao().deleteCompleted()
        true
    }

    suspend fun clearStudyPlan() = database.withTransaction {
        database.focusTimerStateDao().clear()
        database.studyTaskDao().deleteAll()
    }

    suspend fun toggleComplete(id: String) = database.withTransaction {
        val task = database.studyTaskDao().getById(id) ?: return@withTransaction
        if (task.status == "completed") {
            database.studyTaskDao().update(
                task.copy(status = "pending", completedAtMillis = null),
            )
            database.focusSessionDao().deleteStudyPlanSessions(id)
        } else {
            val now = System.currentTimeMillis()
            val subject = database.subjectDao().getById(task.subjectId)
            database.studyTaskDao().update(
                task.copy(
                    status = "completed",
                    completedAtMillis = now,
                    queuePosition = null,
                ),
            )
            database.focusSessionDao().insert(
                FocusSessionEntity(
                    id = "session_${UUID.randomUUID().toString().take(8)}",
                    taskId = task.id,
                    taskTitle = task.title,
                    subjectId = task.subjectId,
                    subjectName = if (subject?.isDefault == true) "Other" else subject?.name ?: "Other",
                    source = "study_plan",
                    durationSeconds = task.focusDurationMinutes * 60,
                    completedAtMillis = now,
                ),
            )
        }
    }

    suspend fun duplicateTask(id: String) = database.withTransaction {
        val task = database.studyTaskDao().getById(id) ?: return@withTransaction
        if (task.status == "completed") return@withTransaction

        database.studyTaskDao().shiftPositionsAfter(task.position)
        database.studyTaskDao().insert(
            task.copy(
                id = "task_${UUID.randomUUID().toString().take(8)}",
                status = "pending",
                position = task.position + 1,
                queuePosition = null,
                completedAtMillis = null,
            ),
        )
    }

    suspend fun moveTask(current: TaskWithSubject, target: TaskWithSubject) = database.withTransaction {
        database.studyTaskDao().updatePosition(current.task.id, target.task.position)
        database.studyTaskDao().updatePosition(target.task.id, current.task.position)
    }

    suspend fun startTask(id: String): Boolean = database.withTransaction {
        val task = database.studyTaskDao().getById(id) ?: return@withTransaction false
        if (task.status == "completed") return@withTransaction false

        database.studyTaskDao().resetActiveTasks()
        database.studyTaskDao().clearQueue()
        database.focusTimerStateDao().clear()
        database.studyTaskDao().update(task.copy(status = "active", queuePosition = 0))
        true
    }

    suspend fun startPlan(): Boolean = database.withTransaction {
        val pending = database.studyTaskDao().getPendingOrdered()
        if (pending.isEmpty()) return@withTransaction false

        database.studyTaskDao().resetActiveTasks()
        database.studyTaskDao().clearQueue()
        database.focusTimerStateDao().clear()
        pending.forEachIndexed { index, task ->
            database.studyTaskDao().setQueuePosition(task.id, index)
        }
        database.studyTaskDao().update(pending.first().copy(status = "active", queuePosition = 0))
        true
    }

    suspend fun getFocusTimerState(): FocusTimerStateEntity? =
        database.focusTimerStateDao().get()

    suspend fun saveFocusTimerState(state: FocusTimerStateEntity) {
        database.focusTimerStateDao().save(state)
    }

    suspend fun clearFocusTimerState() {
        database.focusTimerStateDao().clear()
    }

    suspend fun completeActiveFocus(
        taskId: String,
        durationSeconds: Int,
        awaySeconds: Int,
        startedAtMillis: Long?,
    ): FocusAdvanceResult = database.withTransaction {
        val task = database.studyTaskDao().getById(taskId)
            ?: return@withTransaction FocusAdvanceResult()
        if (task.status != "active") return@withTransaction FocusAdvanceResult()

        val now = System.currentTimeMillis()
        val subject = database.subjectDao().getById(task.subjectId)
        val next = task.queuePosition?.let { database.studyTaskDao().getNextQueued(it) }

        database.studyTaskDao().update(
            task.copy(
                status = "completed",
                completedAtMillis = now,
                queuePosition = null,
            ),
        )
        database.focusSessionDao().insert(
            FocusSessionEntity(
                id = "session_${UUID.randomUUID().toString().take(8)}",
                taskId = task.id,
                taskTitle = task.title,
                subjectId = task.subjectId,
                subjectName = if (subject?.isDefault == true) "Other" else subject?.name ?: "Other",
                source = "study_plan",
                durationSeconds = durationSeconds.coerceAtLeast(0),
                awaySeconds = awaySeconds.coerceAtLeast(0),
                startedAtMillis = startedAtMillis,
                completedAtMillis = now,
            ),
        )

        if (next != null) {
            database.studyTaskDao().update(next.copy(status = "active"))
        } else {
            database.studyTaskDao().clearQueue()
        }

        FocusAdvanceResult(
            nextTaskId = next?.id,
            breakMinutes = if (next == null) 0 else task.breakMinutes,
            planCompleted = next == null,
        )
    }

    suspend fun stopFocusPlan() = database.withTransaction {
        database.studyTaskDao().resetActiveTasks()
        database.studyTaskDao().clearQueue()
        database.focusTimerStateDao().clear()
    }

    suspend fun recordPomodoroFocus(durationSeconds: Int, completedAtMillis: Long = System.currentTimeMillis()) {
        database.focusSessionDao().insert(
            FocusSessionEntity(
                id = "session_${UUID.randomUUID().toString().take(8)}",
                taskTitle = "Pomodoro",
                source = "regular_pomodoro",
                durationSeconds = durationSeconds.coerceAtLeast(0),
                startedAtMillis = completedAtMillis - durationSeconds.coerceAtLeast(0) * 1_000L,
                completedAtMillis = completedAtMillis,
            ),
        )
    }

    suspend fun clearStatistics() {
        database.focusSessionDao().deleteAll()
    }

    companion object {
        const val OTHER_SUBJECT_ID = "subject_other"
        val SUBJECT_COLORS = listOf(
            "#A78BFA",
            "#F472B6",
            "#60A5FA",
            "#34D399",
            "#FBBF24",
            "#FB7185",
            "#22D3EE",
            "#C084FC",
        )
    }
}

data class FocusAdvanceResult(
    val nextTaskId: String? = null,
    val breakMinutes: Int = 0,
    val planCompleted: Boolean = true,
)

package com.sklabs.nagomi.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String? = null,
    val nameKey: String? = null,
    val color: String = "#A78BFA",
    val isDefault: Boolean = false,
    val position: Int = 0,
)

@Entity(
    tableName = "study_tasks",
    indices = [Index("subjectId"), Index("status"), Index("position")],
)
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val subjectId: String = "subject_other",
    val title: String,
    val focusDurationMinutes: Int,
    val breakMinutes: Int,
    val priority: String = "medium",
    val status: String = "pending",
    val position: Int = 0,
    val queuePosition: Int? = null,
    val completedAtMillis: Long? = null,
    val hiddenFromPlan: Boolean = false,
    val hiddenFromCompleted: Boolean = false,
)

@Entity(
    tableName = "focus_sessions",
    indices = [Index("taskId"), Index("subjectId"), Index("completedAtMillis")],
)
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val taskId: String? = null,
    val taskTitle: String,
    val subjectId: String = "subject_other",
    val subjectName: String = "Other",
    val mode: String = "focus",
    val source: String,
    val durationSeconds: Int,
    val awaySeconds: Int = 0,
    val startedAtMillis: Long? = null,
    val completedAtMillis: Long,
    val completed: Boolean = true,
)

@Entity(tableName = "focus_timer_state")
data class FocusTimerStateEntity(
    @PrimaryKey val id: Int = 1,
    val taskId: String,
    val phase: String = "focus",
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val isRunning: Boolean = false,
    val endTimestampMillis: Long? = null,
    val focusStartedAtMillis: Long? = null,
    val awaySeconds: Int = 0,
    val backgroundStartedAtMillis: Long? = null,
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

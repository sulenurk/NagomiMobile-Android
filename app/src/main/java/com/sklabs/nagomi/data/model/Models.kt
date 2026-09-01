package com.sklabs.nagomi.data.model

data class Subject(
    val id: String,
    val name: String? = null,
    val nameKey: String? = null,
    val color: String = "#A78BFA",
    val isDefault: Boolean = false,
)

data class StudyTask(
    val id: String,
    val subjectId: String = "subject_other",
    val subjectName: String = "",
    val title: String,
    val focusDurationMinutes: Int,
    val breakMinutes: Int,
    val priority: String = "medium",
    val status: String = "pending",
    val completedAt: String? = null,
    val hiddenFromPlan: Boolean = false,
    val hiddenFromCompleted: Boolean = false,
)

data class FocusSession(
    val id: String,
    val taskId: String? = null,
    val taskTitle: String,
    val subjectId: String = "subject_other",
    val subjectName: String = "Other",
    val mode: String = "focus",
    val source: String,
    val durationSeconds: Int,
    val awaySeconds: Int = 0,
    val startedAt: String? = null,
    val completedAt: String,
    val completed: Boolean = true,
)

data class AppSettings(
    val autoStartBreak: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val alarmSound: String = "beep",
    val dailyFocusGoalMinutes: Int = 300,
    val regularFocusMinutes: Int = 25,
    val regularShortBreakMinutes: Int = 5,
    val regularLongBreakMinutes: Int = 15,
    val regularLongBreakAfter: Int = 4,
    val regularFocusCount: Int = 4,
    val showQueueProgress: Boolean = true,
    val showCumulativeAwayTime: Boolean = true,
    val weekStartDay: String = "monday",
    val appearanceMode: String = "dark",
    val colorPalette: String = "purple",
    val language: String = "en",
)

package com.sklabs.nagomi.ui.localization

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import org.json.JSONObject

class NagomiStrings private constructor(private val values: JSONObject) {
    fun text(key: String, fallback: String): String = values.optString(key).takeIf { it.isNotBlank() } ?: fallback

    fun format(key: String, fallback: String, vararg values: Pair<String, Any>): String {
        var result = text(key, fallback)
        values.forEach { (name, value) -> result = result.replace("{$name}", value.toString()) }
        return result
    }

    fun status(value: String): String = when {
        value.isBlank() -> ""
        value == "Paused" -> text("paused", value)
        value == "Ready" || value == "Ready to focus" -> text("focus_ready", value)
        value == "Ready for break" -> text("break_ready", value)
        value == "Pomodoro cycle completed" -> text("pomodoro_cycle_completed", value)
        value == "Study plan started" -> text("study_plan_started", value)
        value == "Study plan completed" -> text("study_plan_completed", value)
        value == "Plan stopped" -> text("plan_stopped", value)
        value == "No tasks to start" -> text("no_tasks_to_start", value)
        value == "Task added" -> text("task_added", value)
        value == "Task updated" -> text("task_updated", value)
        value == "Edit mode" -> text("edit_mode", value)
        value == "Completed tasks cleared" -> text("completed_tasks_cleared", value)
        value == "No completed tasks" -> text("no_completed_tasks", value)
        value == "Study plan cleared" -> text("study_plan_cleared", value)
        value == "Subject added" -> text("subject_added", value)
        value == "Subject deleted" -> text("subject_deleted", value)
        value == "Subject name is required" -> text("subject_name_required", value)
        value == "A subject with this name already exists" -> text("subject_already_exists", value)
        value == "Settings saved" -> text("settings_saved", value)
        value == "Pomodoro settings saved" -> text("pomodoro_settings_saved", value)
        value == "Settings reset" -> text("settings_reset", value)
        value == "Statistics cleared" -> text("statistics_cleared", value)
        value == "Select a task from Study Plan" -> text("select_task_from_plan", value)
        value == "Focus reset" -> text("focus_reset", value)
        value == "Break reset" -> text("break_reset", value)
        value == "Timer restored" -> text("timer_restored", value)
        value == "Focus in progress" -> text("focus_in_progress", value)
        value == "Break in progress" -> text("break_in_progress", value)
        value == "Break before the next task" -> text("break_before_next_task", value)
        value == "Durations must be numbers" || value == "Pomodoro settings must be numbers" -> text("durations_must_be_numbers", value)
        value == "Focus must be above zero and break cannot be negative" -> text("invalid_focus_break_duration", value)
        value == "Durations must be above zero; cycle count can be zero or more" -> text("invalid_pomodoro_settings", value)
        value == "Daily goal must be a number above zero" -> text("invalid_daily_goal", value)
        value == "Legacy NagomiMobile data imported" -> text("legacy_data_imported", value)
        value == "This subject already exists" -> text("subject_already_exists", value)
        value.startsWith("Ready for ") -> format("ready_for_task", "Ready for {task}", "task" to value.removePrefix("Ready for "))
        else -> value
    }

    companion object {
        fun empty(): NagomiStrings = NagomiStrings(JSONObject())
        fun load(context: Context, language: String): NagomiStrings {
            val safeLanguage = language.takeIf { candidate ->
                SUPPORTED_LANGUAGES.any { it.first == candidate }
            } ?: "en"
            val json = runCatching {
                context.assets.open("locales/$safeLanguage.json").bufferedReader().use { it.readText() }
            }.getOrElse { "{}" }
            return NagomiStrings(JSONObject(json))
        }

        val SUPPORTED_LANGUAGES = listOf(
            "en" to "English",
            "tr" to "Türkçe",
            "de" to "Deutsch",
            "fr" to "Français",
            "es" to "Español",
        )
    }
}

val LocalNagomiStrings = staticCompositionLocalOf { NagomiStrings.empty() }

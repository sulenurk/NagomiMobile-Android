package com.sklabs.nagomi.data.migration

import android.content.Context
import androidx.room.withTransaction
import com.sklabs.nagomi.data.local.FocusSessionEntity
import com.sklabs.nagomi.data.local.FocusTimerStateEntity
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.local.StudyTaskEntity
import com.sklabs.nagomi.data.local.SubjectEntity
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.timer.PomodoroSnapshot
import com.sklabs.nagomi.timer.PomodoroStateStore
import com.sklabs.nagomi.timer.TimerMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import org.json.JSONObject

class LegacyDataMigrator(private val context: Context) {
    private val preferences = context.getSharedPreferences(MIGRATION_PREFERENCES, Context.MODE_PRIVATE)

    suspend fun migrateIfAvailable(): Boolean {
        if (preferences.getBoolean(KEY_COMPLETE, false)) return false
        val source = findLegacyFile() ?: return false
        val root = runCatching { JSONObject(source.readText(Charsets.UTF_8)) }.getOrNull() ?: return false
        val database = NagomiDatabase.getInstance(context)

        database.withTransaction {
            val subjects = root.optJSONArray("subjects")
            if (subjects != null) {
                for (index in 0 until subjects.length()) {
                    val item = subjects.optJSONObject(index) ?: continue
                    val id = item.optString("id").ifBlank { "subject_legacy_$index" }
                    database.subjectDao().insert(
                        SubjectEntity(
                            id = id,
                            name = item.optString("name").takeIf(String::isNotBlank),
                            nameKey = item.optString("name_key").takeIf(String::isNotBlank),
                            color = item.optString("color", "#A78BFA"),
                            isDefault = item.optBoolean("is_default", id == "subject_other"),
                            position = index,
                        ),
                    )
                }
            }

            val queueIds = root.optJSONArray("queue_task_ids")?.let { array ->
                (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
            }.orEmpty()
            val activeId = root.nullableString("active_task_id")
            val tasks = root.optJSONArray("tasks")
            if (tasks != null) {
                for (index in 0 until tasks.length()) {
                    val item = tasks.optJSONObject(index) ?: continue
                    val id = item.optString("id").ifBlank { "task_legacy_$index" }
                    database.studyTaskDao().insert(
                        StudyTaskEntity(
                            id = id,
                            subjectId = item.optString("subject_id", "subject_other"),
                            title = item.optString("title", item.optString("task_name", "New task")),
                            focusDurationMinutes = item.optInt("focus_duration", 25).coerceAtLeast(1),
                            breakMinutes = item.optInt("break_minutes", 5).coerceAtLeast(0),
                            priority = item.optString("priority", "medium"),
                            status = if (id == activeId) "active" else item.optString("status", "pending"),
                            position = index,
                            queuePosition = queueIds.indexOf(id).takeIf { it >= 0 },
                            completedAtMillis = parseDate(item.optString("completed_at")),
                            hiddenFromPlan = item.optBoolean("hidden_from_plan", false),
                            hiddenFromCompleted = item.optBoolean("hidden_from_completed", false),
                        ),
                    )
                }
            }

            val sessions = root.optJSONArray("sessions")
            if (sessions != null) {
                for (index in 0 until sessions.length()) {
                    val item = sessions.optJSONObject(index) ?: continue
                    val completedAt = parseDate(item.optString("completed_at")) ?: System.currentTimeMillis()
                    database.focusSessionDao().insert(
                        FocusSessionEntity(
                            id = item.optString("id").ifBlank { "session_legacy_$index" },
                            taskId = item.nullableString("task_id"),
                            taskTitle = item.optString("task_title", "Focus"),
                            subjectId = item.optString("subject_id", "subject_other"),
                            subjectName = item.optString("subject_name", "Other"),
                            mode = item.optString("mode", "focus"),
                            source = item.optString("source", "study_plan"),
                            durationSeconds = item.optInt("duration_seconds", 0).coerceAtLeast(0),
                            awaySeconds = item.optInt("away_seconds", 0).coerceAtLeast(0),
                            startedAtMillis = parseDate(item.optString("started_at")),
                            completedAtMillis = completedAt,
                            completed = item.optBoolean("completed", true),
                        ),
                    )
                }
            }

            migrateFocusState(root.optJSONObject("focus_timer_state"), database)
        }

        migrateSettings(root)
        migratePomodoroState(root.optJSONObject("regular_pomodoro_state"))
        preferences.edit().putBoolean(KEY_COMPLETE, true).putString(KEY_SOURCE, source.absolutePath).apply()
        return true
    }

    private fun migrateSettings(root: JSONObject) {
        val legacy = root.optJSONObject("settings") ?: JSONObject()
        val language = root.optString("language", "en")
        SettingsRepository(context).update { current ->
            current.copy(
                autoStartBreak = legacy.optBoolean("auto_start_break", current.autoStartBreak),
                autoStartFocus = legacy.optBoolean("auto_start_focus", current.autoStartFocus),
                soundEnabled = legacy.optBoolean("sound_enabled", current.soundEnabled),
                vibrationEnabled = legacy.optBoolean("vibration_enabled", current.vibrationEnabled),
                alarmSound = legacy.optString("alarm_sound", current.alarmSound),
                dailyFocusGoalMinutes = legacy.optInt("daily_focus_goal_minutes", current.dailyFocusGoalMinutes),
                regularFocusMinutes = legacy.optInt("regular_focus_minutes", current.regularFocusMinutes),
                regularShortBreakMinutes = legacy.optInt("regular_short_break_minutes", current.regularShortBreakMinutes),
                regularLongBreakMinutes = legacy.optInt("regular_long_break_minutes", current.regularLongBreakMinutes),
                regularLongBreakAfter = legacy.optInt("regular_long_break_after", current.regularLongBreakAfter),
                regularFocusCount = legacy.optInt("regular_focus_count", current.regularFocusCount),
                showQueueProgress = legacy.optBoolean("show_queue_progress", current.showQueueProgress),
                showCumulativeAwayTime = legacy.optBoolean("show_cumulative_away_time", current.showCumulativeAwayTime),
                weekStartDay = legacy.optString("week_start_day", current.weekStartDay),
                appearanceMode = legacy.optString("appearance_mode", current.appearanceMode),
                colorPalette = legacy.optString("color_palette", current.colorPalette),
                language = language,
            )
        }
    }

    private fun migratePomodoroState(state: JSONObject?) {
        if (state == null) return
        val mode = when (state.optString("mode")) {
            "short_break" -> TimerMode.SHORT_BREAK
            "long_break" -> TimerMode.LONG_BREAK
            else -> TimerMode.FOCUS
        }
        PomodoroStateStore(context).write(
            PomodoroSnapshot(
                mode = mode,
                completedFocusCount = state.optInt("completed_focus_count", 0),
                remainingSeconds = state.optInt("remaining_seconds", 0),
                endTimestampMillis = timestampMillis(state.optDouble("end_timestamp", -1.0)),
                isRunning = state.optBoolean("is_running", false),
                isPaused = state.optBoolean("is_paused", false),
                cycleCompleted = state.optBoolean("cycle_completed", false),
            ),
        )
    }

    private suspend fun migrateFocusState(state: JSONObject?, database: NagomiDatabase) {
        if (state == null) return
        val taskId = state.nullableString("active_task_id") ?: return
        val mode = state.optString("current_mode", "focus")
        val total = if (mode == "break") state.optInt("break_seconds", 300) else state.optInt("focus_seconds", 1500)
        database.focusTimerStateDao().save(
            FocusTimerStateEntity(
                taskId = taskId,
                phase = if (mode == "break") "break" else "focus",
                remainingSeconds = state.optInt("remaining_seconds", total),
                totalSeconds = total,
                isRunning = state.optBoolean("is_running", false),
                endTimestampMillis = timestampMillis(state.optDouble("timer_end_timestamp", -1.0)),
                focusStartedAtMillis = parseDate(state.optString("session_started_at")),
                awaySeconds = state.optInt("away_seconds", 0),
            ),
        )
    }

    private fun findLegacyFile(): File? = context.filesDir.walkTopDown()
        .maxDepth(4)
        .firstOrNull { it.isFile && it.name == "app_data.json" }

    private fun parseDate(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(value)?.time
        }.getOrNull()
    }

    private fun timestampMillis(value: Double): Long? = value.takeIf { it > 0.0 }?.let {
        if (it > 10_000_000_000.0) it.toLong() else (it * 1_000.0).toLong()
    }

    private fun JSONObject.nullableString(key: String): String? {
        if (isNull(key)) return null
        return optString(key).trim().takeIf { it.isNotEmpty() && it != "null" }
    }

    companion object {
        private const val MIGRATION_PREFERENCES = "legacy_data_migration"
        private const val KEY_COMPLETE = "app_data_json_imported"
        private const val KEY_SOURCE = "source_path"
    }
}

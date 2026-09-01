package com.sklabs.nagomi.timer

import android.content.Context

class PomodoroStateStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun read(): PomodoroSnapshot? {
        if (!preferences.getBoolean(KEY_EXISTS, false)) return null
        val mode = runCatching {
            TimerMode.valueOf(preferences.getString(KEY_MODE, TimerMode.FOCUS.name) ?: TimerMode.FOCUS.name)
        }.getOrDefault(TimerMode.FOCUS)
        val end = preferences.getLong(KEY_END, -1L).takeIf { it > 0L }
        return PomodoroSnapshot(
            mode = mode,
            completedFocusCount = preferences.getInt(KEY_COMPLETED, 0),
            remainingSeconds = preferences.getInt(KEY_REMAINING, 0),
            endTimestampMillis = end,
            isRunning = preferences.getBoolean(KEY_RUNNING, false) && end != null,
            isPaused = preferences.getBoolean(KEY_PAUSED, false),
            cycleCompleted = preferences.getBoolean(KEY_CYCLE_COMPLETED, false),
        )
    }

    fun write(snapshot: PomodoroSnapshot) {
        preferences.edit()
            .putBoolean(KEY_EXISTS, true)
            .putString(KEY_MODE, snapshot.mode.name)
            .putInt(KEY_COMPLETED, snapshot.completedFocusCount)
            .putInt(KEY_REMAINING, snapshot.remainingSeconds)
            .putLong(KEY_END, snapshot.endTimestampMillis ?: -1L)
            .putBoolean(KEY_RUNNING, snapshot.isRunning)
            .putBoolean(KEY_PAUSED, snapshot.isPaused)
            .putBoolean(KEY_CYCLE_COMPLETED, snapshot.cycleCompleted)
            .apply()
    }

    companion object {
        private const val NAME = "pomodoro_state"
        private const val KEY_EXISTS = "exists"
        private const val KEY_MODE = "mode"
        private const val KEY_COMPLETED = "completed_focus_count"
        private const val KEY_REMAINING = "remaining_seconds"
        private const val KEY_END = "end_timestamp"
        private const val KEY_RUNNING = "is_running"
        private const val KEY_PAUSED = "is_paused"
        private const val KEY_CYCLE_COMPLETED = "cycle_completed"
    }
}

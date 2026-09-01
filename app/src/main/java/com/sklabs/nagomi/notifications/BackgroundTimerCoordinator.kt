package com.sklabs.nagomi.notifications

import android.content.Context
import com.sklabs.nagomi.data.local.FocusTimerStateEntity
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.repository.NagomiRepository
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.timer.PomodoroEngine
import com.sklabs.nagomi.timer.PomodoroSettings
import com.sklabs.nagomi.timer.PomodoroStateStore
import com.sklabs.nagomi.timer.TimerMode
import com.sklabs.nagomi.ui.localization.NagomiStrings

class BackgroundTimerCoordinator(context: Context) {
    private val application = context.applicationContext
    private val database = NagomiDatabase.getInstance(application)
    private val repository = NagomiRepository(database)
    private val settingsRepository = SettingsRepository(application)
    private val scheduler = NativeTimerScheduler(application)
    private val pomodoroStore = PomodoroStateStore(application)

    suspend fun handleFinished(kind: NativeTimerKind) {
        when (kind) {
            NativeTimerKind.POMODORO -> finishPomodoro()
            NativeTimerKind.FOCUS -> finishFocus()
        }
    }

    private suspend fun finishPomodoro() {
        val stored = pomodoroStore.read() ?: return
        if (!stored.isRunning || stored.endTimestampMillis == null) return
        val now = System.currentTimeMillis()
        if (stored.endTimestampMillis > now + 1_000L) return

        val settings = settingsRepository.settings.value
        val engine = PomodoroEngine(
            PomodoroSettings(
                focusMinutes = settings.regularFocusMinutes,
                shortBreakMinutes = settings.regularShortBreakMinutes,
                longBreakMinutes = settings.regularLongBreakMinutes,
                longBreakAfter = settings.regularLongBreakAfter,
                focusCount = settings.regularFocusCount,
                autoStartBreak = settings.autoStartBreak,
                autoStartFocus = settings.autoStartFocus,
            ),
        )
        engine.restore(stored)
        if (!engine.sync(now)) return

        val completedFocusSeconds = if (engine.mode == TimerMode.FOCUS) engine.totalSeconds else 0
        val result = engine.finishCurrentSession()
        if (result.focusCompleted) repository.recordPomodoroFocus(completedFocusSeconds, now)
        if (result.shouldAutoStart) engine.start(now)
        pomodoroStore.write(engine.snapshot())
        scheduler.finishOngoingNotification(NativeTimerKind.POMODORO)
        if (engine.isRunning) schedulePomodoro(engine)
    }

    private suspend fun finishFocus() {
        val stored = database.focusTimerStateDao().get() ?: return
        val end = stored.endTimestampMillis ?: return
        if (!stored.isRunning || end > System.currentTimeMillis() + 1_000L) return

        scheduler.finishOngoingNotification(NativeTimerKind.FOCUS)
        val settings = settingsRepository.settings.value
        if (stored.phase == "break") {
            val task = database.studyTaskDao().getById(stored.taskId) ?: run {
                database.focusTimerStateDao().clear()
                return
            }
            val total = task.focusDurationMinutes * 60
            val running = settings.autoStartFocus
            val now = System.currentTimeMillis()
            database.focusTimerStateDao().save(
                FocusTimerStateEntity(
                    taskId = task.id,
                    phase = "focus",
                    remainingSeconds = total,
                    totalSeconds = total,
                    isRunning = running,
                    endTimestampMillis = if (running) now + total * 1_000L else null,
                    focusStartedAtMillis = if (running) now else null,
                ),
            )
            if (running) scheduleFocus(task.title, "focus", total, now)
            return
        }

        val task = database.studyTaskDao().getById(stored.taskId) ?: return
        val now = System.currentTimeMillis()
        val extraAway = stored.backgroundStartedAtMillis?.let {
            ((minOf(now, end) - it).coerceAtLeast(0L) / 1_000L).toInt()
        } ?: 0
        val result = repository.completeActiveFocus(
            taskId = task.id,
            durationSeconds = stored.totalSeconds,
            awaySeconds = stored.awaySeconds + extraAway,
            startedAtMillis = stored.focusStartedAtMillis,
        )
        val nextId = result.nextTaskId
        if (result.planCompleted || nextId == null) {
            database.focusTimerStateDao().clear()
            return
        }
        val next = database.studyTaskDao().getById(nextId) ?: return
        if (result.breakMinutes > 0) {
            val total = result.breakMinutes * 60
            val running = settings.autoStartBreak
            database.focusTimerStateDao().save(
                FocusTimerStateEntity(
                    taskId = next.id,
                    phase = "break",
                    remainingSeconds = total,
                    totalSeconds = total,
                    isRunning = running,
                    endTimestampMillis = if (running) now + total * 1_000L else null,
                ),
            )
            if (running) scheduleFocus(next.title, "break", total, now)
        } else {
            val total = next.focusDurationMinutes * 60
            val running = settings.autoStartFocus
            database.focusTimerStateDao().save(
                FocusTimerStateEntity(
                    taskId = next.id,
                    phase = "focus",
                    remainingSeconds = total,
                    totalSeconds = total,
                    isRunning = running,
                    endTimestampMillis = if (running) now + total * 1_000L else null,
                    focusStartedAtMillis = if (running) now else null,
                ),
            )
            if (running) scheduleFocus(next.title, "focus", total, now)
        }
    }

    private fun schedulePomodoro(engine: PomodoroEngine) {
        val snapshot = engine.snapshot()
        val end = snapshot.endTimestampMillis ?: return
        val strings = NagomiStrings.load(application, settingsRepository.settings.value.language)
        val mode = when (snapshot.mode) {
            TimerMode.FOCUS -> strings.text("focus_mode", "Focus")
            TimerMode.SHORT_BREAK -> strings.text("short_break_mode", "Short break")
            TimerMode.LONG_BREAK -> strings.text("long_break_mode", "Long break")
        }
        val alarmTitle = when (snapshot.mode) {
            TimerMode.FOCUS -> strings.text("focus_session_finished", "Focus complete")
            TimerMode.SHORT_BREAK -> strings.text("short_break_finished", "Short break finished")
            TimerMode.LONG_BREAK -> strings.text("long_break_finished", "Long break finished")
        }
        scheduler.schedule(
            kind = NativeTimerKind.POMODORO,
            endTimestampMillis = end,
            notificationTitle = "Pomodoro · $mode",
            notificationText = "${strings.text("current_cycle", "Cycle")} ${snapshot.completedFocusCount + 1}",
            alarmTitle = alarmTitle,
        )
    }

    private fun scheduleFocus(title: String, phase: String, total: Int, now: Long) {
        val strings = NagomiStrings.load(application, settingsRepository.settings.value.language)
        scheduler.schedule(
            kind = NativeTimerKind.FOCUS,
            endTimestampMillis = now + total * 1_000L,
            notificationTitle = if (phase == "focus") {
                "${strings.text("focus_mode", "Focus")} · $title"
            } else {
                "${strings.text("break_mode", "Break")} · ${strings.format("next_task", "next: {task}", "task" to title)}"
            },
            notificationText = strings.text("timer_countdown", "Nagomi countdown"),
            alarmTitle = if (phase == "focus") {
                "${strings.text("focus_timer_completed", "Focus complete")} · $title"
            } else {
                strings.text("break_finished", "Break complete")
            },
        )
    }
}

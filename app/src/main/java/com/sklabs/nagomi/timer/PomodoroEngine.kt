package com.sklabs.nagomi.timer

import kotlin.math.ceil

enum class TimerMode {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
}

data class PomodoroSettings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val longBreakAfter: Int = 4,
    val focusCount: Int = 4,
    val autoStartBreak: Boolean = false,
    val autoStartFocus: Boolean = false,
) {
    init {
        require(focusMinutes > 0)
        require(shortBreakMinutes > 0)
        require(longBreakMinutes > 0)
        require(longBreakAfter > 0)
        require(focusCount >= 0)
    }
}

data class PomodoroSnapshot(
    val mode: TimerMode,
    val completedFocusCount: Int,
    val remainingSeconds: Int,
    val endTimestampMillis: Long?,
    val isRunning: Boolean,
    val isPaused: Boolean,
    val cycleCompleted: Boolean,
)

data class SessionResult(
    val finishedMode: TimerMode,
    val focusCompleted: Boolean,
    val cycleCompleted: Boolean,
    val shouldAutoStart: Boolean,
)

class PomodoroEngine(
    var settings: PomodoroSettings = PomodoroSettings(),
) {
    var mode: TimerMode = TimerMode.FOCUS
        private set
    var completedFocusCount: Int = 0
        private set
    var remainingSeconds: Int = durationFor(TimerMode.FOCUS)
        private set
    var endTimestampMillis: Long? = null
        private set
    var isRunning: Boolean = false
        private set
    var isPaused: Boolean = false
        private set
    var cycleCompleted: Boolean = false
        private set

    val totalSeconds: Int
        get() = durationFor(mode)

    val progress: Float
        get() = if (totalSeconds <= 0) 0f
        else ((totalSeconds - remainingSeconds).toFloat() / totalSeconds).coerceIn(0f, 1f)

    fun start(nowMillis: Long = System.currentTimeMillis()) {
        if (cycleCompleted) reset()
        if (isRunning) return

        endTimestampMillis = nowMillis + remainingSeconds * 1_000L
        isRunning = true
        isPaused = false
    }

    fun pause(nowMillis: Long = System.currentTimeMillis()) {
        if (!isRunning) return
        sync(nowMillis)
        endTimestampMillis = null
        isRunning = false
        isPaused = true
    }

    fun reset() {
        mode = TimerMode.FOCUS
        completedFocusCount = 0
        remainingSeconds = durationFor(TimerMode.FOCUS)
        endTimestampMillis = null
        isRunning = false
        isPaused = false
        cycleCompleted = false
    }

    fun skip() {
        endTimestampMillis = null
        isRunning = false
        isPaused = false
        cycleCompleted = false
        advanceMode()
    }

    fun sync(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val end = endTimestampMillis ?: return false
        if (!isRunning) return false

        remainingSeconds = ceil((end - nowMillis).coerceAtLeast(0L) / 1_000.0).toInt()
        if (remainingSeconds > 0) return false

        endTimestampMillis = null
        isRunning = false
        isPaused = false
        return true
    }

    fun finishCurrentSession(): SessionResult {
        val finishedMode = mode
        val focusCompleted = finishedMode == TimerMode.FOCUS

        if (focusCompleted) {
            completedFocusCount += 1
            if (settings.focusCount > 0 && completedFocusCount >= settings.focusCount) {
                cycleCompleted = true
                remainingSeconds = 0
                return SessionResult(finishedMode, true, true, false)
            }
        }

        advanceMode()
        val shouldAutoStart = when (mode) {
            TimerMode.FOCUS -> settings.autoStartFocus
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> settings.autoStartBreak
        }
        return SessionResult(finishedMode, focusCompleted, false, shouldAutoStart)
    }

    fun updateSettings(newSettings: PomodoroSettings) {
        settings = newSettings
        if (!isRunning && !isPaused) reset()
    }

    fun snapshot(): PomodoroSnapshot = PomodoroSnapshot(
        mode = mode,
        completedFocusCount = completedFocusCount,
        remainingSeconds = remainingSeconds,
        endTimestampMillis = endTimestampMillis,
        isRunning = isRunning,
        isPaused = isPaused,
        cycleCompleted = cycleCompleted,
    )

    fun restore(snapshot: PomodoroSnapshot) {
        mode = snapshot.mode
        completedFocusCount = snapshot.completedFocusCount.coerceAtLeast(0)
        remainingSeconds = snapshot.remainingSeconds.coerceAtLeast(0)
        endTimestampMillis = snapshot.endTimestampMillis
        isRunning = snapshot.isRunning && snapshot.endTimestampMillis != null
        isPaused = snapshot.isPaused && !isRunning
        cycleCompleted = snapshot.cycleCompleted
    }

    private fun durationFor(targetMode: TimerMode): Int = when (targetMode) {
        TimerMode.FOCUS -> settings.focusMinutes * 60
        TimerMode.SHORT_BREAK -> settings.shortBreakMinutes * 60
        TimerMode.LONG_BREAK -> settings.longBreakMinutes * 60
    }

    private fun advanceMode() {
        mode = if (mode == TimerMode.FOCUS) {
            val longBreakDue = completedFocusCount > 0 &&
                completedFocusCount % settings.longBreakAfter == 0
            if (longBreakDue) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK
        } else {
            TimerMode.FOCUS
        }
        remainingSeconds = durationFor(mode)
    }
}

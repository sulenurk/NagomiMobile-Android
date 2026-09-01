package com.sklabs.nagomi.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.repository.NagomiRepository
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.notifications.NativeTimerKind
import com.sklabs.nagomi.notifications.NativeTimerScheduler
import com.sklabs.nagomi.ui.localization.NagomiStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PomodoroUiState(
    val snapshot: PomodoroSnapshot,
    val progress: Float,
    val focusCount: Int,
    val status: String = "",
)

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val stateStore = PomodoroStateStore(application)
    private val engine = PomodoroEngine(settingsRepository.settings.value.toPomodoroSettings()).apply {
        stateStore.read()?.let(::restore)
    }
    private val nativeTimer = NativeTimerScheduler(application)
    private val repository = NagomiRepository(NagomiDatabase.getInstance(application))
    private val _uiState = MutableStateFlow(engine.toUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()
    private var ticker: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                val updated = PomodoroSettings(
                    focusMinutes = settings.regularFocusMinutes,
                    shortBreakMinutes = settings.regularShortBreakMinutes,
                    longBreakMinutes = settings.regularLongBreakMinutes,
                    longBreakAfter = settings.regularLongBreakAfter,
                    focusCount = settings.regularFocusCount,
                    autoStartBreak = settings.autoStartBreak,
                    autoStartFocus = settings.autoStartFocus,
                )
                if (updated != engine.settings) engine.updateSettings(updated)
                publish()
            }
        }
    }

    fun startOrPause() {
        if (engine.isRunning) {
            engine.pause()
            stopTicker()
            nativeTimer.cancel(NativeTimerKind.POMODORO)
            publish("Paused")
        } else {
            engine.start()
            scheduleNativeTimer()
            startTicker()
            publish()
        }
    }

    fun pauseIfRunning() {
        if (!engine.isRunning) return
        engine.pause()
        stopTicker()
        nativeTimer.cancel(NativeTimerKind.POMODORO)
        publish("Paused")
    }

    fun reset() {
        stopTicker()
        nativeTimer.cancel(NativeTimerKind.POMODORO)
        engine.reset()
        publish()
    }

    fun skip() {
        stopTicker()
        nativeTimer.cancel(NativeTimerKind.POMODORO)
        engine.skip()
        publish(readyMessage())
    }

    private fun startTicker() {
        stopTicker()
        ticker = viewModelScope.launch {
            while (isActive && engine.isRunning) {
                val finished = engine.sync()
                if (finished) {
                    finishExpiredSession()
                } else {
                    publish()
                }
                delay(250)
            }
        }
    }

    fun onAppForegrounded() {
        viewModelScope.launch {
            stateStore.read()?.let(engine::restore)
            if (!engine.isRunning) return@launch
            val finished = engine.sync()
            if (finished) finishExpiredSession() else {
                scheduleNativeTimer()
                publish()
            }
        }
    }

    fun refreshNativeTimer() {
        if (engine.isRunning) scheduleNativeTimer()
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    private fun readyMessage(): String = when (engine.mode) {
        TimerMode.FOCUS -> "Ready to focus"
        TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> "Ready for break"
    }

    private fun scheduleNativeTimer() {
        val snapshot = engine.snapshot()
        val end = snapshot.endTimestampMillis ?: return
        val strings = NagomiStrings.load(getApplication(), settingsRepository.settings.value.language)
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
        nativeTimer.schedule(
            kind = NativeTimerKind.POMODORO,
            endTimestampMillis = end,
            notificationTitle = "Pomodoro · $mode",
            notificationText = "${strings.text("current_cycle", "Cycle")} ${snapshot.completedFocusCount + 1}",
            alarmTitle = alarmTitle,
        )
    }

    private suspend fun finishExpiredSession() {
        nativeTimer.finishOngoingNotification(NativeTimerKind.POMODORO)
        val completedFocusSeconds = if (engine.mode == TimerMode.FOCUS) engine.totalSeconds else 0
        val result = engine.finishCurrentSession()
        if (result.focusCompleted) {
            repository.recordPomodoroFocus(completedFocusSeconds)
        }
        if (result.shouldAutoStart) {
            engine.start()
            scheduleNativeTimer()
        }
        publish(if (result.cycleCompleted) "Pomodoro cycle completed" else readyMessage())
    }

    private fun publish(status: String = _uiState.value.status) {
        val snapshot = engine.snapshot()
        stateStore.write(snapshot)
        _uiState.value = engine.toUiState(status)
    }

    private fun PomodoroEngine.toUiState(status: String = "") = PomodoroUiState(
        snapshot = snapshot(),
        progress = progress,
        focusCount = settings.focusCount,
        status = status,
    )

    private fun com.sklabs.nagomi.data.model.AppSettings.toPomodoroSettings() = PomodoroSettings(
        focusMinutes = regularFocusMinutes,
        shortBreakMinutes = regularShortBreakMinutes,
        longBreakMinutes = regularLongBreakMinutes,
        longBreakAfter = regularLongBreakAfter,
        focusCount = regularFocusCount,
        autoStartBreak = autoStartBreak,
        autoStartFocus = autoStartFocus,
    )
}

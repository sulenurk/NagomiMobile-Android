package com.sklabs.nagomi.ui.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sklabs.nagomi.data.local.FocusTimerStateEntity
import com.sklabs.nagomi.data.local.NagomiDatabase
import com.sklabs.nagomi.data.local.TaskWithSubject
import com.sklabs.nagomi.data.repository.NagomiRepository
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.notifications.NativeTimerKind
import com.sklabs.nagomi.notifications.NativeTimerScheduler
import com.sklabs.nagomi.ui.localization.NagomiStrings
import kotlin.math.ceil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class FocusPhase {
    FOCUS,
    BREAK,
}

data class FocusUiState(
    val currentTask: TaskWithSubject? = null,
    val queue: List<TaskWithSubject> = emptyList(),
    val phase: FocusPhase = FocusPhase.FOCUS,
    val timerTaskId: String? = null,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isRunning: Boolean = false,
    val endTimestampMillis: Long? = null,
    val focusStartedAtMillis: Long? = null,
    val awaySeconds: Int = 0,
    val backgroundStartedAtMillis: Long? = null,
    val loading: Boolean = true,
    val planCompleted: Boolean = false,
    val status: String = "",
) {
    val progress: Float
        get() = if (totalSeconds <= 0) 0f
        else ((totalSeconds - remainingSeconds).toFloat() / totalSeconds).coerceIn(0f, 1f)
}

class FocusViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NagomiRepository(NagomiDatabase.getInstance(application))
    private val nativeTimer = NativeTimerScheduler(application)
    private val settingsRepository = SettingsRepository(application)
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var ticker: Job? = null
    private var restored = false
    private var appSettings = AppSettings()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { appSettings = it }
        }
        viewModelScope.launch {
            repository.ensureDefaultSubject()
            combine(repository.activeTask, repository.focusQueue) { active, queue -> active to queue }
                .collect { (active, queue) ->
                    _uiState.update { it.copy(currentTask = active, queue = queue, loading = false) }
                    if (!restored) {
                        restored = true
                        restoreTimer(active)
                    } else if (active != null && _uiState.value.timerTaskId != active.task.id) {
                        createFocusTimer(active, "Ready to focus")
                    }
                }
        }
    }

    fun startOrPause() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.timerTaskId == null || state.totalSeconds <= 0) return@launch
            if (state.isRunning) pauseTimer() else startTimer()
        }
    }

    fun pauseIfRunning() {
        if (!_uiState.value.isRunning) return
        viewModelScope.launch { pauseTimer() }
    }

    fun resetPhase() {
        viewModelScope.launch {
            stopTicker()
            nativeTimer.cancel(NativeTimerKind.FOCUS)
            _uiState.update {
                it.copy(
                    remainingSeconds = it.totalSeconds,
                    isRunning = false,
                    endTimestampMillis = null,
                    focusStartedAtMillis = if (it.phase == FocusPhase.FOCUS) null else it.focusStartedAtMillis,
                    awaySeconds = if (it.phase == FocusPhase.FOCUS) 0 else it.awaySeconds,
                    backgroundStartedAtMillis = null,
                    status = if (it.phase == FocusPhase.FOCUS) "Focus reset" else "Break reset",
                )
            }
            persistTimer()
        }
    }

    fun completeOrSkipPhase() {
        viewModelScope.launch { finishPhase(automatic = false) }
    }

    fun stopPlan() {
        viewModelScope.launch {
            stopTicker()
            nativeTimer.cancel(NativeTimerKind.FOCUS)
            repository.stopFocusPlan()
            _uiState.value = FocusUiState(
                loading = false,
                status = "Plan stopped",
            )
        }
    }

    fun onAppBackgrounded() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isRunning || state.backgroundStartedAtMillis != null) return@launch
            _uiState.update { it.copy(backgroundStartedAtMillis = System.currentTimeMillis()) }
            persistTimer()
        }
    }

    fun onAppForegrounded() {
        viewModelScope.launch {
            val state = _uiState.value
            val backgroundStarted = state.backgroundStartedAtMillis
            if (backgroundStarted == null) {
                if (state.isRunning) scheduleNativeTimer()
                return@launch
            }
            val now = System.currentTimeMillis()
            val extraAway = if (state.phase == FocusPhase.FOCUS) {
                calculateAwaySeconds(backgroundStarted, state.endTimestampMillis, now)
            } else {
                0
            }
            val remaining = remainingAt(state, now)
            _uiState.update {
                it.copy(
                    remainingSeconds = remaining,
                    awaySeconds = it.awaySeconds + extraAway,
                    backgroundStartedAtMillis = null,
                )
            }
            if (remaining == 0 && state.isRunning) {
                finishPhase(automatic = true)
            } else {
                persistTimer()
                if (state.isRunning) scheduleNativeTimer()
            }
        }
    }

    fun refreshNativeTimer() {
        if (_uiState.value.isRunning) scheduleNativeTimer()
    }

    private suspend fun restoreTimer(active: TaskWithSubject?) {
        val stored = repository.getFocusTimerState()
        if (active == null) {
            if (stored != null) repository.clearFocusTimerState()
            _uiState.update { it.copy(planCompleted = false, status = "Select a task from Study Plan") }
            return
        }
        if (stored == null || stored.taskId != active.task.id) {
            createFocusTimer(active, "Ready to focus")
            return
        }

        val now = System.currentTimeMillis()
        val phase = if (stored.phase == "break") FocusPhase.BREAK else FocusPhase.FOCUS
        val extraAway = if (phase == FocusPhase.FOCUS && stored.backgroundStartedAtMillis != null) {
            calculateAwaySeconds(stored.backgroundStartedAtMillis, stored.endTimestampMillis, now)
        } else {
            0
        }
        val remaining = if (stored.isRunning && stored.endTimestampMillis != null) {
            ceil((stored.endTimestampMillis - now).coerceAtLeast(0L) / 1_000.0).toInt()
        } else {
            stored.remainingSeconds
        }

        _uiState.update {
            it.copy(
                phase = phase,
                timerTaskId = stored.taskId,
                remainingSeconds = remaining,
                totalSeconds = stored.totalSeconds,
                isRunning = stored.isRunning,
                endTimestampMillis = stored.endTimestampMillis,
                focusStartedAtMillis = stored.focusStartedAtMillis,
                awaySeconds = stored.awaySeconds + extraAway,
                backgroundStartedAtMillis = null,
                status = if (stored.isRunning) "Timer restored" else "Ready",
            )
        }
        if (stored.isRunning && remaining == 0) {
            finishPhase(automatic = true)
        } else {
            persistTimer()
            if (stored.isRunning) {
                scheduleNativeTimer()
                startTicker()
            }
        }
    }

    private suspend fun createFocusTimer(task: TaskWithSubject, status: String) {
        stopTicker()
        val total = task.task.focusDurationMinutes * 60
        _uiState.update {
            it.copy(
                currentTask = task,
                phase = FocusPhase.FOCUS,
                timerTaskId = task.task.id,
                remainingSeconds = total,
                totalSeconds = total,
                isRunning = false,
                endTimestampMillis = null,
                focusStartedAtMillis = null,
                awaySeconds = 0,
                backgroundStartedAtMillis = null,
                planCompleted = false,
                status = status,
            )
        }
        persistTimer()
    }

    private suspend fun startTimer() {
        val now = System.currentTimeMillis()
        val end = now + _uiState.value.remainingSeconds * 1_000L
        _uiState.update {
            it.copy(
                isRunning = true,
                endTimestampMillis = end,
                focusStartedAtMillis = if (it.phase == FocusPhase.FOCUS) it.focusStartedAtMillis ?: now else it.focusStartedAtMillis,
                backgroundStartedAtMillis = null,
                status = if (it.phase == FocusPhase.FOCUS) "Focus in progress" else "Break in progress",
            )
        }
        persistTimer()
        scheduleNativeTimer()
        startTicker()
    }

    private suspend fun pauseTimer() {
        val state = _uiState.value
        val remaining = remainingAt(state, System.currentTimeMillis())
        stopTicker()
        nativeTimer.cancel(NativeTimerKind.FOCUS)
        _uiState.update {
            it.copy(
                remainingSeconds = remaining,
                isRunning = false,
                endTimestampMillis = null,
                backgroundStartedAtMillis = null,
                status = "Paused",
            )
        }
        persistTimer()
    }

    private fun startTicker() {
        stopTicker()
        ticker = viewModelScope.launch {
            val end = _uiState.value.endTimestampMillis
                ?: (System.currentTimeMillis() + _uiState.value.remainingSeconds * 1_000L)
            while (isActive && _uiState.value.isRunning) {
                val remaining = ceil((end - System.currentTimeMillis()).coerceAtLeast(0L) / 1_000.0).toInt()
                _uiState.update { it.copy(remainingSeconds = remaining) }
                if (remaining == 0) {
                    ticker = null
                    finishPhase(automatic = true)
                    break
                }
                delay(250)
            }
        }
    }

    private suspend fun finishPhase(automatic: Boolean) {
        val state = _uiState.value
        val taskId = state.timerTaskId ?: return
        stopTicker()
        if (automatic) {
            nativeTimer.finishOngoingNotification(NativeTimerKind.FOCUS)
        } else {
            nativeTimer.cancel(NativeTimerKind.FOCUS)
        }

        if (state.phase == FocusPhase.BREAK) {
            val active = state.currentTask ?: return
            createFocusTimer(active, "Ready for ${active.task.title}")
            if (automatic && appSettings.autoStartFocus) startTimer()
            return
        }

        val elapsed = if (automatic) state.totalSeconds else state.totalSeconds - state.remainingSeconds
        val awayAtCompletion = state.awaySeconds + if (state.backgroundStartedAtMillis != null) {
            calculateAwaySeconds(
                state.backgroundStartedAtMillis,
                state.endTimestampMillis,
                System.currentTimeMillis(),
            )
        } else {
            0
        }
        val result = repository.completeActiveFocus(
            taskId = taskId,
            durationSeconds = elapsed,
            awaySeconds = awayAtCompletion,
            startedAtMillis = state.focusStartedAtMillis,
        )

        if (result.planCompleted || result.nextTaskId == null) {
            repository.clearFocusTimerState()
            _uiState.update {
                it.copy(
                    currentTask = null,
                    queue = emptyList(),
                    timerTaskId = null,
                    remainingSeconds = 0,
                    totalSeconds = 0,
                    isRunning = false,
                    endTimestampMillis = null,
                    backgroundStartedAtMillis = null,
                    planCompleted = true,
                    status = "Study plan completed",
                )
            }
            return
        }

        if (result.breakMinutes > 0) {
            val total = result.breakMinutes * 60
            _uiState.update {
                it.copy(
                    phase = FocusPhase.BREAK,
                    timerTaskId = result.nextTaskId,
                    remainingSeconds = total,
                    totalSeconds = total,
                    isRunning = false,
                    endTimestampMillis = null,
                    focusStartedAtMillis = null,
                    awaySeconds = 0,
                    backgroundStartedAtMillis = null,
                    status = "Break before the next task",
                )
            }
            persistTimer()
            if (automatic && appSettings.autoStartBreak) startTimer()
        } else {
            val next = _uiState.value.queue.firstOrNull { it.task.id == result.nextTaskId }
            if (next != null) {
                createFocusTimer(next, "Ready for ${next.task.title}")
                if (automatic && appSettings.autoStartFocus) startTimer()
            }
        }
    }

    private fun remainingAt(state: FocusUiState, now: Long): Int {
        if (!state.isRunning) return state.remainingSeconds
        val storedEnd = state.endTimestampMillis ?: return state.remainingSeconds
        return ceil((storedEnd - now).coerceAtLeast(0L) / 1_000.0).toInt()
    }

    private fun calculateAwaySeconds(backgroundStarted: Long, endTimestamp: Long?, now: Long): Int {
        val intervalEnd = minOf(now, endTimestamp ?: now)
        return ((intervalEnd - backgroundStarted).coerceAtLeast(0L) / 1_000L).toInt()
    }

    private suspend fun persistTimer() {
        val state = _uiState.value
        val taskId = state.timerTaskId ?: return
        repository.saveFocusTimerState(
            FocusTimerStateEntity(
                taskId = taskId,
                phase = if (state.phase == FocusPhase.BREAK) "break" else "focus",
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSeconds,
                isRunning = state.isRunning,
                endTimestampMillis = if (state.isRunning) state.endTimestampMillis else null,
                focusStartedAtMillis = state.focusStartedAtMillis,
                awaySeconds = state.awaySeconds,
                backgroundStartedAtMillis = state.backgroundStartedAtMillis,
            ),
        )
    }

    private fun scheduleNativeTimer() {
        val state = _uiState.value
        val end = state.endTimestampMillis ?: return
        val taskTitle = state.currentTask?.task?.title ?: "Study Plan"
        val strings = NagomiStrings.load(getApplication(), appSettings.language)
        nativeTimer.schedule(
            kind = NativeTimerKind.FOCUS,
            endTimestampMillis = end,
            notificationTitle = if (state.phase == FocusPhase.FOCUS) {
                "${strings.text("focus_mode", "Focus")} · $taskTitle"
            } else {
                "${strings.text("break_mode", "Break")} · ${strings.format("next_task", "next: {task}", "task" to taskTitle)}"
            },
            notificationText = strings.text("timer_countdown", "Nagomi countdown"),
            alarmTitle = if (state.phase == FocusPhase.FOCUS) {
                "${strings.text("focus_timer_completed", "Focus complete")} · $taskTitle"
            } else {
                strings.text("break_finished", "Break complete")
            },
        )
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }
}

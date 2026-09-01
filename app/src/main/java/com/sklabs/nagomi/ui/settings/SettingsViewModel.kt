package com.sklabs.nagomi.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sklabs.nagomi.data.model.AppSettings
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.notifications.AlarmPlaybackService
import com.sklabs.nagomi.notifications.AlarmSoundCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SystemAccessState(
    val notificationsEnabled: Boolean = true,
    val exactAlarmsEnabled: Boolean = true,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    val settings = repository.settings

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    private val _systemAccess = MutableStateFlow(SystemAccessState())
    val systemAccess = _systemAccess.asStateFlow()

    private var previewPlayer: MediaPlayer? = null

    init {
        refreshSystemAccess()
    }

    fun setAutoStartFocus(enabled: Boolean) = update { copy(autoStartFocus = enabled) }
    fun setAutoStartBreak(enabled: Boolean) = update { copy(autoStartBreak = enabled) }

    fun setSoundEnabled(enabled: Boolean) {
        update { copy(soundEnabled = enabled) }
        if (!enabled) {
            stopPreview()
            AlarmPlaybackService.stop(getApplication<Application>())
        }
    }

    fun setVibrationEnabled(enabled: Boolean) = update { copy(vibrationEnabled = enabled) }
    fun setShowQueueProgress(enabled: Boolean) = update { copy(showQueueProgress = enabled) }
    fun setShowCumulativeAwayTime(enabled: Boolean) = update { copy(showCumulativeAwayTime = enabled) }
    fun setWeekStartDay(day: String) = update { copy(weekStartDay = day) }
    fun setAppearanceMode(mode: String) = update { copy(appearanceMode = mode) }
    fun setColorPalette(palette: String) = update { copy(colorPalette = palette) }
    fun setLanguage(language: String) = update { copy(language = language) }

    fun setAlarmSound(key: String) {
        update { copy(alarmSound = key) }
        if (settings.value.soundEnabled) previewAlarm(key)
    }

    fun saveDailyGoal(text: String) {
        val minutes = text.toIntOrNull()
        if (minutes == null || minutes <= 0) {
            _status.value = "Daily goal must be a number above zero"
            return
        }
        update { copy(dailyFocusGoalMinutes = minutes) }
        _status.value = "Settings saved"
    }

    fun savePomodoroSettings(
        focusText: String,
        shortBreakText: String,
        longBreakText: String,
        longBreakAfterText: String,
        focusCountText: String,
    ) {
        val focus = focusText.toIntOrNull()
        val shortBreak = shortBreakText.toIntOrNull()
        val longBreak = longBreakText.toIntOrNull()
        val longBreakAfter = longBreakAfterText.toIntOrNull()
        val focusCount = focusCountText.toIntOrNull()
        if (
            focus == null || shortBreak == null || longBreak == null ||
            longBreakAfter == null || focusCount == null
        ) {
            _status.value = "Pomodoro settings must be numbers"
            return
        }
        if (focus <= 0 || shortBreak <= 0 || longBreak <= 0 || longBreakAfter <= 0 || focusCount < 0) {
            _status.value = "Durations must be above zero; cycle count can be zero or more"
            return
        }
        update {
            copy(
                regularFocusMinutes = focus,
                regularShortBreakMinutes = shortBreak,
                regularLongBreakMinutes = longBreak,
                regularLongBreakAfter = longBreakAfter,
                regularFocusCount = focusCount,
            )
        }
        _status.value = "Pomodoro settings saved"
    }

    fun resetSettings() {
        stopPreview()
        repository.reset()
        _status.value = "Settings reset"
    }

    fun refreshSystemAccess() {
        val application = getApplication<Application>()
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val notifications = permissionGranted &&
            NotificationManagerCompat.from(application).areNotificationsEnabled()
        val exactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            application.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        } else {
            true
        }
        _systemAccess.value = SystemAccessState(notifications, exactAlarms)
    }

    fun openNotificationSettings() {
        val application = getApplication<Application>()
        application.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, application.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val application = getApplication<Application>()
        runCatching {
            application.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${application.packageName}"),
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
            )
        }
    }

    override fun onCleared() {
        stopPreview()
        super.onCleared()
    }

    private fun update(transform: AppSettings.() -> AppSettings) {
        repository.update(transform)
        _status.value = ""
    }

    private fun previewAlarm(key: String) {
        stopPreview()
        val application = getApplication<Application>()
        previewPlayer = runCatching {
            val descriptor = application.resources.openRawResourceFd(AlarmSoundCatalog.resourceId(key))
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                isLooping = false
                setOnCompletionListener { stopPreview() }
                prepare()
                start()
            }
        }.getOrNull()
    }

    private fun stopPreview() {
        previewPlayer?.runCatching { stop() }
        previewPlayer?.release()
        previewPlayer = null
    }
}

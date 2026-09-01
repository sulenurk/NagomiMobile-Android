package com.sklabs.nagomi.notifications

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sklabs.nagomi.MainActivity
import com.sklabs.nagomi.R
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.ui.localization.NagomiStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AlarmPlaybackState(
    val active: Boolean = false,
    val title: String = "",
    val timerKind: String? = null,
)

class AlarmPlaybackService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { stopSelf() }

    override fun onCreate() {
        super.onCreate()
        NativeTimerScheduler.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(NativeTimerScheduler.EXTRA_ALARM_TITLE)
            ?.takeIf(String::isNotBlank)
            ?: "Nagomi timer complete"
        val timerKind = intent?.getStringExtra(NativeTimerScheduler.EXTRA_TIMER_KIND)
        _state.value = AlarmPlaybackState(true, title, timerKind)
        startForeground(NativeTimerScheduler.ALARM_NOTIFICATION_ID, buildNotification(title, timerKind))
        startPlayback()
        handler.removeCallbacks(autoStop)
        handler.postDelayed(autoStop, 60_000L)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        _state.value = AlarmPlaybackState()
        mediaPlayer?.runCatching { stop() }
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        handler.removeCallbacks(autoStop)
        super.onDestroy()
    }

    private fun buildNotification(title: String, timerKind: String?): Notification {
        val settings = SettingsRepository.read(this)
        val strings = NagomiStrings.load(this, settings.language)
        val contentIntent = PendingIntent.getActivity(
            this,
            9201,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NativeTimerScheduler.EXTRA_TIMER_KIND, timerKind)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getBroadcast(
            this,
            9202,
            Intent(this, TimerActionReceiver::class.java).apply {
                action = NativeTimerScheduler.ACTION_STOP_ALARM
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, NativeTimerScheduler.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(title)
            .setContentText(strings.text("tap_stop_alarm", "Tap Stop alarm to silence Nagomi"))
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_timer_notification, strings.text("stop_alarm", "Stop alarm"), stopIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    private fun startPlayback() {
        if (mediaPlayer?.isPlaying == true) return
        val settings = SettingsRepository.read(this)
        if (settings.soundEnabled || settings.vibrationEnabled) acquireWakeLock()
        if (settings.soundEnabled) {
            mediaPlayer = runCatching {
                val descriptor = resources.openRawResourceFd(
                    AlarmSoundCatalog.resourceId(settings.alarmSound),
                )
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    descriptor.close()
                    isLooping = true
                    prepare()
                    start()
                }
            }.getOrNull()
        }
        if (settings.vibrationEnabled) startVibration()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Nagomi:TimerAlarm",
        ).apply { acquire(10 * 60 * 1_000L) }
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0L, 500L, 350L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator?.vibrate(pattern, 0)
        }
    }

    companion object {
        private val _state = MutableStateFlow(AlarmPlaybackState())
        val state: StateFlow<AlarmPlaybackState> = _state.asStateFlow()

        fun start(context: Context, title: String, timerKind: String?): Boolean = runCatching {
            val intent = Intent(context, AlarmPlaybackService::class.java).apply {
                putExtra(NativeTimerScheduler.EXTRA_ALARM_TITLE, title)
                putExtra(NativeTimerScheduler.EXTRA_TIMER_KIND, timerKind)
            }
            ContextCompat.startForegroundService(context, intent)
        }.isSuccess

        fun stop(context: Context) {
            _state.value = AlarmPlaybackState()
            context.stopService(Intent(context, AlarmPlaybackService::class.java))
        }
    }
}

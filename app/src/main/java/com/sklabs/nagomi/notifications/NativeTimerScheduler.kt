package com.sklabs.nagomi.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sklabs.nagomi.MainActivity
import com.sklabs.nagomi.R
import com.sklabs.nagomi.data.settings.SettingsRepository
import com.sklabs.nagomi.ui.localization.NagomiStrings

enum class NativeTimerKind(
    val key: String,
    val notificationId: Int,
) {
    POMODORO("pomodoro", 2101),
    FOCUS("focus", 2102),
}

class NativeTimerScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    init {
        createNotificationChannels(context)
    }

    fun schedule(
        kind: NativeTimerKind,
        endTimestampMillis: Long,
        notificationTitle: String,
        notificationText: String,
        alarmTitle: String,
    ): Boolean {
        cancelScheduledAlarm(kind)

        val operation = alarmPendingIntent(kind, endTimestampMillis, alarmTitle)
        val exactAccess = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        val exactScheduled = exactAccess && runCatching {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endTimestampMillis,
                operation,
            )
        }.isSuccess
        if (!exactScheduled) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endTimestampMillis,
                operation,
            )
        }

        preferences.edit().putLong(endKey(kind), endTimestampMillis).apply()
        showCountdownNotification(kind, endTimestampMillis, notificationTitle, notificationText)
        return exactScheduled
    }

    fun cancel(kind: NativeTimerKind) {
        cancelScheduledAlarm(kind)
        NotificationManagerCompat.from(context).cancel(kind.notificationId)
        NotificationManagerCompat.from(context).cancel(ALARM_NOTIFICATION_ID)
        AlarmPlaybackService.stop(context)
        preferences.edit().remove(endKey(kind)).apply()
    }

    fun finishOngoingNotification(kind: NativeTimerKind) {
        NotificationManagerCompat.from(context).cancel(kind.notificationId)
        preferences.edit().remove(endKey(kind)).apply()
    }

    private fun cancelScheduledAlarm(kind: NativeTimerKind) {
        val previousEnd = preferences.getLong(endKey(kind), -1L)
        if (previousEnd > 0L) {
            alarmManager.cancel(alarmPendingIntent(kind, previousEnd, ""))
        }
    }

    private fun alarmPendingIntent(
        kind: NativeTimerKind,
        endTimestampMillis: Long,
        alarmTitle: String,
    ): PendingIntent {
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            action = ACTION_TIMER_FINISHED
            data = Uri.parse("nagomi://timer/${kind.key}/$endTimestampMillis")
            putExtra(EXTRA_TIMER_KIND, kind.key)
            putExtra(EXTRA_ALARM_TITLE, alarmTitle)
        }
        return PendingIntent.getBroadcast(
            context,
            kind.notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun showCountdownNotification(
        kind: NativeTimerKind,
        endTimestampMillis: Long,
        title: String,
        text: String,
    ) {
        if (!canPostNotifications(context)) return
        val contentIntent = PendingIntent.getActivity(
            context,
            9001,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setWhen(endTimestampMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(kind.notificationId, notification) }
    }

    companion object {
        const val ACTION_TIMER_FINISHED = "com.sklabs.nagomi.action.TIMER_FINISHED"
        const val ACTION_STOP_ALARM = "com.sklabs.nagomi.action.STOP_ALARM"
        const val EXTRA_TIMER_KIND = "timer_kind"
        const val EXTRA_ALARM_TITLE = "alarm_title"
        const val TIMER_CHANNEL_ID = "nagomi_running_timers"
        const val ALARM_CHANNEL_ID = "nagomi_alarm_playback"
        const val FALLBACK_ALARM_CHANNEL_ID = "nagomi_alarm_fallback"
        const val ALARM_NOTIFICATION_ID = 2201
        private const val PREFERENCES = "native_timer_alarms"

        private fun endKey(kind: NativeTimerKind) = "${kind.key}_end"

        fun canPostNotifications(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            val runningChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Running timers",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Countdown shown while a Nagomi timer is running"
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Timer alarms",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Active Nagomi timer alarms"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val alarmAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            val fallbackChannel = NotificationChannel(
                FALLBACK_ALARM_CHANNEL_ID,
                "Timer alarm fallback",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Fallback alarm when continuous playback cannot start"
                setSound(alarmUri, alarmAttributes)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannels(listOf(runningChannel, alarmChannel, fallbackChannel))
        }

        fun showFallbackAlarm(context: Context, title: String) {
            if (!canPostNotifications(context)) return
            createNotificationChannels(context)
            val settings = SettingsRepository.read(context)
            val strings = NagomiStrings.load(context, settings.language)
            val channel = if (settings.soundEnabled) FALLBACK_ALARM_CHANNEL_ID else ALARM_CHANNEL_ID
            val notification = NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_timer_notification)
                .setContentTitle(title)
                .setContentText(strings.text("session_completed", "Timer finished"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setSilent(!settings.soundEnabled)
                .build()
            runCatching {
                NotificationManagerCompat.from(context).notify(ALARM_NOTIFICATION_ID, notification)
            }
        }
    }
}

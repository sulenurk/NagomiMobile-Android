package com.sklabs.nagomi.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.sklabs.nagomi.AppRuntimeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val kindKey = intent.getStringExtra(NativeTimerScheduler.EXTRA_TIMER_KIND)
        val kind = NativeTimerKind.entries.firstOrNull { it.key == kindKey }
        kind?.let {
            NotificationManagerCompat.from(context).cancel(kind.notificationId)
        }
        val title = intent.getStringExtra(NativeTimerScheduler.EXTRA_ALARM_TITLE)
            ?.takeIf(String::isNotBlank)
            ?: "Nagomi timer complete"
        if (!AlarmPlaybackService.start(context, title, kindKey)) {
            NativeTimerScheduler.showFallbackAlarm(context, title)
        }
        if (kind != null && !AppRuntimeState.hasUiProcess) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    BackgroundTimerCoordinator(context).handleFinished(kind)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

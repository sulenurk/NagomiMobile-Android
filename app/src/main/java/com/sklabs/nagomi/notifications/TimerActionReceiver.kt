package com.sklabs.nagomi.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class TimerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NativeTimerScheduler.ACTION_STOP_ALARM) return
        AlarmPlaybackService.stop(context)
        NotificationManagerCompat.from(context).cancel(NativeTimerScheduler.ALARM_NOTIFICATION_ID)
    }
}

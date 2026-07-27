package com.teamodoro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.teamodoro.R
import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
import com.teamodoro.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            "Timer",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Ongoing timer notification"
            setShowBadge(false)
        }

        val transitionChannel = NotificationChannel(
            CHANNEL_TRANSITION,
            "Phase Transitions",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts when the timer phase changes"
        }

        manager.createNotificationChannel(timerChannel)
        manager.createNotificationChannel(transitionChannel)
    }

    fun buildTimerNotification(state: TimerState): Notification {
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val phaseLabel = if (state.phase == TimerPhase.WORK) "Focus" else "Break"
        val timeText = state.remainingMillis.toMinutesSeconds()

        return NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_tile_timer)
            .setContentTitle("$phaseLabel — $timeText remaining")
            .setContentText("Teamodoro is running")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showTransitionNotification(newPhase: TimerPhase) {
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = if (newPhase == TimerPhase.WORK) "Focus time!" else "Take a break!"
        val text = if (newPhase == TimerPhase.WORK) "100-minute focus session started" else "30-minute break started"

        val notification = NotificationCompat.Builder(context, CHANNEL_TRANSITION)
            .setSmallIcon(R.drawable.ic_tile_timer)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(NOTIFICATION_TRANSITION_ID, notification)
    }

    private fun Long.toMinutesSeconds(): String {
        val totalSeconds = this / 1_000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    companion object {
        const val CHANNEL_TIMER = "teamodoro_timer"
        const val CHANNEL_TRANSITION = "teamodoro_transition"
        const val NOTIFICATION_TIMER_ID = 1
        private const val NOTIFICATION_TRANSITION_ID = 2
    }
}

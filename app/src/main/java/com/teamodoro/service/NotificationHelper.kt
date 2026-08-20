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
import com.teamodoro.locale.LocaleManager
import com.teamodoro.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localeManager: LocaleManager,
) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun createChannels(localizedContext: Context) {
        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            localizedContext.getString(R.string.notification_channel_timer_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = localizedContext.getString(R.string.notification_channel_timer_description)
            setShowBadge(false)
        }

        val transitionChannel = NotificationChannel(
            CHANNEL_TRANSITION,
            localizedContext.getString(R.string.notification_channel_transition_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = localizedContext.getString(R.string.notification_channel_transition_description)
        }

        manager.createNotificationChannel(timerChannel)
        manager.createNotificationChannel(transitionChannel)
    }

    fun buildTimerNotification(state: TimerState): Notification {
        val localizedContext = localeManager.localizedContext()
        createChannels(localizedContext)
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val content = NotificationContent.timer(state, localizedCopy(localizedContext))

        return NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_tile_timer)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showTransitionNotification(newPhase: TimerPhase) {
        val localizedContext = localeManager.localizedContext()
        createChannels(localizedContext)
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val content = NotificationContent.transition(newPhase, localizedCopy(localizedContext))

        val notification = NotificationCompat.Builder(context, CHANNEL_TRANSITION)
            .setSmallIcon(R.drawable.ic_tile_timer)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(NOTIFICATION_TRANSITION_ID, notification)
    }

    private fun localizedCopy(context: Context) = NotificationCopy(
        focusLabel = context.getString(R.string.phase_focus),
        breakLabel = context.getString(R.string.phase_break),
        timerTitleFormat = context.getString(R.string.notification_timer_title),
        timerText = context.getString(R.string.notification_timer_text),
        focusTransitionTitle = context.getString(R.string.notification_focus_start_title),
        focusTransitionText = context.getString(R.string.notification_focus_start_text),
        breakTransitionTitle = context.getString(R.string.notification_break_start_title),
        breakTransitionText = context.getString(R.string.notification_break_start_text),
    )

    companion object {
        const val CHANNEL_TIMER = "teamodoro_timer"
        const val CHANNEL_TRANSITION = "teamodoro_transition"
        const val NOTIFICATION_TIMER_ID = 1
        private const val NOTIFICATION_TRANSITION_ID = 2
    }
}

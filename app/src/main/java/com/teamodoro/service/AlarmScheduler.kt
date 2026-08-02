package com.teamodoro.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNextTransition(triggerAtMillis: Long) {
        val pendingIntent = checkNotNull(
            transitionPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fall back to inexact alarm when exact alarms are not permitted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel() {
        val pendingIntent = transitionPendingIntent(
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }

    /**
     * PendingIntent matching includes the wrapped Intent action. Both scheduling
     * and cancellation must therefore build this exact same request.
     */
    private fun transitionPendingIntent(flags: Int): PendingIntent? {
        val identity = transitionRequestIdentity
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = identity.action
        }
        return PendingIntent.getBroadcast(context, identity.requestCode, intent, flags)
    }

    companion object {
        const val ACTION_PHASE_TRANSITION = "com.teamodoro.ACTION_PHASE_TRANSITION"
        private const val REQUEST_CODE = 1001

        internal val transitionRequestIdentity = AlarmRequestIdentity(
            receiverClassName = AlarmReceiver::class.java.name,
            action = ACTION_PHASE_TRANSITION,
            requestCode = REQUEST_CODE,
        )
    }
}

/** The Android fields that determine the canonical transition alarm request. */
internal data class AlarmRequestIdentity(
    val receiverClassName: String,
    val action: String,
    val requestCode: Int,
)

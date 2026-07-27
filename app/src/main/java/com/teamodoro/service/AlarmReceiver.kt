package com.teamodoro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Wakes the device at a phase boundary and hands off to [TimerService].
 *
 * No Hilt entry point and no injected [AlarmScheduler]: this receiver only
 * forwards an intent. It previously injected the scheduler and never used it.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_PHASE_TRANSITION) return

        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PHASE_TRANSITION
        }
        // The service runs in the foreground and we are being invoked from the
        // background, so plain startService would throw on API 26+.
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

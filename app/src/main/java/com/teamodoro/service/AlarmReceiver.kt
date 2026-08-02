package com.teamodoro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.teamodoro.data.TimerPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Wakes the device at a phase boundary and hands off to [TimerService].
 *
 * A transition alarm may have been queued just before the user stopped
 * tracking. Check the persisted preference before reviving the service so that
 * stale broadcasts cannot restore notifications.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferences: TimerPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (!TrackingPolicy.shouldHandlePhaseTransition(intent.action, preferences.isTracking)) return

        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PHASE_TRANSITION
        }
        // The service runs in the foreground and we are being invoked from the
        // background, so plain startService would throw on API 26+.
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

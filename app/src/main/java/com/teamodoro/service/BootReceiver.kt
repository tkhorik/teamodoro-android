package com.teamodoro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.teamodoro.data.TimerPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Restores tracking after a reboot — but only if the user had it switched on.
 * Alarms do not survive a reboot, so the service has to re-arm them itself.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferences: TimerPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!preferences.isTracking) return

        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}

package com.teamodoro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Restart foreground service after reboot so alarms are rescheduled
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        context.startService(serviceIntent)
    }
}

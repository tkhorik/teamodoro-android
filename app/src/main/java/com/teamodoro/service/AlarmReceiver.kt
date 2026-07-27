package com.teamodoro.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_PHASE_TRANSITION) return

        // Notify the foreground service about the phase transition
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PHASE_TRANSITION
        }
        context.startService(serviceIntent)
    }
}

package com.teamodoro.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerState
import com.teamodoro.service.NotificationHelper.Companion.NOTIFICATION_TIMER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : LifecycleService() {

    @Inject
    lateinit var calculateTimerUseCase: CalculateTimerUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private var timerJob: Job? = null
    private var lastState: TimerState = TimerState.DEFAULT

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIFICATION_TIMER_ID,
            notificationHelper.buildTimerNotification(TimerState.DEFAULT),
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
            ACTION_PHASE_TRANSITION -> handlePhaseTransition()
        }

        return START_STICKY
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = calculateTimerUseCase.timerStateFlow()
            .onEach { state ->
                updateNotification(state)
                scheduleAlarmIfPhaseChanged(state)
                lastState = state
            }
            .launchIn(lifecycleScope)

        // Schedule the first alarm
        lifecycleScope.launch {
            val offset = calculateTimerUseCase.let {
                // Use current offset for initial alarm scheduling
                val currentTime = System.currentTimeMillis()
                val millis = it.millisUntilNextTransition(currentTime, lastState.cyclePosition)
                currentTime + millis
            }
            alarmScheduler.scheduleNextTransition(offset)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        alarmScheduler.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handlePhaseTransition() {
        notificationHelper.showTransitionNotification(lastState.phase)

        // Reschedule alarm for the next transition
        val nextTransitionMillis = System.currentTimeMillis() + lastState.remainingMillis
        alarmScheduler.scheduleNextTransition(nextTransitionMillis)
    }

    private fun updateNotification(state: TimerState) {
        val notification = notificationHelper.buildTimerNotification(state)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_TIMER_ID, notification)
    }

    private fun scheduleAlarmIfPhaseChanged(newState: TimerState) {
        if (newState.phase != lastState.phase) {
            notificationHelper.showTransitionNotification(newState.phase)
            val nextTransitionMillis = System.currentTimeMillis() + newState.remainingMillis
            alarmScheduler.scheduleNextTransition(nextTransitionMillis)
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.teamodoro.ACTION_START"
        const val ACTION_STOP = "com.teamodoro.ACTION_STOP"
        const val ACTION_PHASE_TRANSITION = "com.teamodoro.ACTION_PHASE_TRANSITION"
    }
}

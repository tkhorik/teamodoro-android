package com.teamodoro.service

import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.teamodoro.data.TimerPreferences
import com.teamodoro.domain.CYCLE_MILLIS
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerState
import com.teamodoro.service.NotificationHelper.Companion.NOTIFICATION_TIMER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : LifecycleService() {

    @Inject
    lateinit var calculateTimerUseCase: CalculateTimerUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var preferences: TimerPreferences

    private var timerJob: Job? = null

    /**
     * Identifies the last transition we already alerted on, as
     * `cycleIndex * 2 + phase.ordinal`. Both the 1-second ticker and the
     * AlarmManager broadcast can observe the same transition, so this keeps the
     * user from getting the alert twice.
     */
    private var lastAlertedTransitionId: Long = NO_TRANSITION

    override fun onCreate() {
        super.onCreate()
        // Seed from the real current phase, not TimerState.DEFAULT. Starting the
        // service mid-break used to show "Focus 25:00" for up to a second and,
        // worse, made the first ticker emission look like a WORK -> BREAK change
        // and fire a spurious "Take a break!" alert.
        val state = calculateTimerUseCase.calculate(System.currentTimeMillis())
        lastAlertedTransitionId = transitionIdFor(System.currentTimeMillis())

        // Android 14+ requires the foreground service type to be passed here and
        // to match the manifest declaration, or the service is killed on start.
        // ServiceCompat handles the pre-29 case where the parameter doesn't exist.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_TIMER_ID,
            notificationHelper.buildTimerNotification(state),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
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
        preferences.isTracking = true

        timerJob?.cancel()
        timerJob = calculateTimerUseCase.timerStateFlow()
            .onEach { state -> onTick(state) }
            .launchIn(lifecycleScope)

        // Arm the first alarm from the clock itself. The previous version passed
        // lastState.cyclePosition where a roomOffset was expected — two different
        // units — and lastState was still DEFAULT at this point, so the first
        // alarm fired at an arbitrary time.
        armAlarm(System.currentTimeMillis())
    }

    private fun stopTimer() {
        preferences.isTracking = false
        timerJob?.cancel()
        timerJob = null
        alarmScheduler.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTick(state: TimerState) {
        updateNotification(state)
        alertIfTransitionUnseen(System.currentTimeMillis(), state)
    }

    /**
     * Called when the AlarmManager broadcast lands, i.e. the process may have been
     * dozing and the ticker was not running.
     */
    private fun handlePhaseTransition() {
        // Exact alarms can fire a few millis early. Reading the clock with a small
        // forward slack makes sure we resolve to the phase that is *starting*,
        // rather than reporting the one that is ending. The previous version read
        // a stale lastState instead, so it announced the wrong phase and then
        // rescheduled the next alarm from an out-of-date remainingMillis.
        val now = System.currentTimeMillis() + BOUNDARY_SLACK_MILLIS
        val state = calculateTimerUseCase.calculate(now)

        updateNotification(state)
        alertIfTransitionUnseen(now, state)
        armAlarm(now)
    }

    private fun alertIfTransitionUnseen(nowMillis: Long, state: TimerState) {
        val id = transitionIdFor(nowMillis)
        if (id == lastAlertedTransitionId) return
        lastAlertedTransitionId = id
        notificationHelper.showTransitionNotification(state.phase)
        // Keep the alarm chain alive even if a previous one was dropped.
        armAlarm(nowMillis)
    }

    private fun armAlarm(nowMillis: Long) {
        alarmScheduler.scheduleNextTransition(calculateTimerUseCase.nextTransitionAt(nowMillis))
    }

    private fun transitionIdFor(nowMillis: Long): Long {
        val cycleIndex = nowMillis.floorDiv(CYCLE_MILLIS)
        val phaseOrdinal = calculateTimerUseCase.calculate(nowMillis).phase.ordinal
        return cycleIndex * 2 + phaseOrdinal
    }

    private fun updateNotification(state: TimerState) {
        val notification = notificationHelper.buildTimerNotification(state)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_TIMER_ID, notification)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.teamodoro.ACTION_START"
        const val ACTION_STOP = "com.teamodoro.ACTION_STOP"
        const val ACTION_PHASE_TRANSITION = "com.teamodoro.ACTION_PHASE_TRANSITION"

        private const val NO_TRANSITION = Long.MIN_VALUE
        private const val BOUNDARY_SLACK_MILLIS = 250L
    }
}

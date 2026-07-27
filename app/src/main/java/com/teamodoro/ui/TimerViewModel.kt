package com.teamodoro.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamodoro.data.TimerPreferences
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerState
import com.teamodoro.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    calculateTimerUseCase: CalculateTimerUseCase,
    preferences: TimerPreferences,
) : ViewModel() {

    val timerState: StateFlow<TimerState> = calculateTimerUseCase
        .timerStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // Seed from the clock, not TimerState.DEFAULT — otherwise the first
            // frame shows "Focus 25:00" regardless of the real phase.
            initialValue = calculateTimerUseCase.calculate(System.currentTimeMillis()),
        )

    /**
     * Whether this device is tracking the cycle (foreground service + ongoing
     * notification). The cycle itself always runs; this only controls whether
     * we are following along.
     */
    val isTracking: StateFlow<Boolean> = preferences.isTrackingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = preferences.isTracking,
        )

    fun startTimer() = sendToService(TimerService.ACTION_START)

    fun stopTimer() = sendToService(TimerService.ACTION_STOP)

    private fun sendToService(serviceAction: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = serviceAction
        }
        ContextCompat.startForegroundService(context, intent)
    }
}

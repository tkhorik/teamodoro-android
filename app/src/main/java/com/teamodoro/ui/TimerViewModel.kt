package com.teamodoro.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamodoro.data.TimerPreferences
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerState
import com.teamodoro.locale.LocaleManager
import com.teamodoro.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    calculateTimerUseCase: CalculateTimerUseCase,
    preferences: TimerPreferences,
    private val localeManager: LocaleManager,
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

    /** All languages the in-app picker offers, "System default" first. */
    val supportedLanguages: List<LocaleManager.AppLanguage> = localeManager.supportedLanguages

    /** True on API 33+, where Settings has its own per-app language screen we can deep-link to. */
    val systemLanguagePickerAvailable: Boolean = localeManager.systemPickerAvailable()

    private val _currentLanguageTag = MutableStateFlow(localeManager.currentTag())

    /** The active language override tag, or null when following the system language. */
    val currentLanguageTag: StateFlow<String?> = _currentLanguageTag.asStateFlow()

    /** Intent for Settings > Apps > Teamodoro > Language. Only meaningful when [systemLanguagePickerAvailable]. */
    fun systemLanguageSettingsIntent(): Intent = localeManager.systemLanguageSettingsIntent()

    /** Applies an in-app language override; pass null to go back to following the system language. */
    fun setLanguage(tag: String?) {
        localeManager.setLanguage(tag)
        _currentLanguageTag.value = tag
    }

    /** Re-read a choice made in Android's per-app language Settings screen. */
    fun refreshCurrentLanguage() {
        _currentLanguageTag.value = localeManager.currentTag()
    }

    fun startTimer() = sendToService(TimerService.ACTION_START)

    fun stopTimer() = sendToService(TimerService.ACTION_STOP)

    private fun sendToService(serviceAction: String) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = serviceAction
        }
        ContextCompat.startForegroundService(context, intent)
    }
}

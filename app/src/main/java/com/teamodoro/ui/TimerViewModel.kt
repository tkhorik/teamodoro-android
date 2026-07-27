package com.teamodoro.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamodoro.data.RoomRepository
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerState
import com.teamodoro.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: RoomRepository,
    private val calculateTimerUseCase: CalculateTimerUseCase,
) : ViewModel() {

    val timerState: StateFlow<TimerState> = calculateTimerUseCase
        .timerStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TimerState.DEFAULT,
        )

    fun startTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        context.startService(intent)
    }

    fun stopTimer() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        context.startService(intent)
    }

    /** Sets the UTC offset received from the server/team room, persisting it locally. */
    fun setRoomOffset(offsetMillis: Long) {
        viewModelScope.launch {
            repository.updateOffset(offsetMillis)
        }
    }

    /** Sets the room ID, then starts the timer. */
    fun joinRoom(roomId: String, offsetMillis: Long) {
        viewModelScope.launch {
            repository.saveRoomConfig(roomId, offsetMillis)
            startTimer()
        }
    }
}

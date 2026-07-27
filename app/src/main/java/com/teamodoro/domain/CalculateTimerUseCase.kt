package com.teamodoro.domain

import com.teamodoro.data.RoomRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CalculateTimerUseCase @Inject constructor(
    private val repository: RoomRepository,
) {
    /** Emits a fresh [TimerState] every second by combining the ticker with the persisted offset. */
    fun timerStateFlow(): Flow<TimerState> {
        val ticker = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(1_000L)
            }
        }
        return combine(ticker, repository.roomOffset) { currentTime, offset ->
            calculate(currentTime, offset)
        }
    }

    /** Calculates [TimerState] for a given wall-clock time and stored room offset. */
    fun calculate(currentTimeMillis: Long, roomOffset: Long): TimerState {
        val position = (currentTimeMillis - roomOffset) % CYCLE_MILLIS
        val normalizedPosition = if (position < 0) position + CYCLE_MILLIS else position

        val phase: TimerPhase
        val remainingMillis: Long
        if (normalizedPosition < WORK_DURATION_MILLIS) {
            phase = TimerPhase.WORK
            remainingMillis = WORK_DURATION_MILLIS - normalizedPosition
        } else {
            phase = TimerPhase.BREAK
            remainingMillis = CYCLE_MILLIS - normalizedPosition
        }

        return TimerState(
            phase = phase,
            remainingMillis = remainingMillis,
            cyclePosition = normalizedPosition,
            isRunning = roomOffset != 0L,
        )
    }

    /** Returns the millis until the next phase transition, used to schedule the next alarm. */
    fun millisUntilNextTransition(currentTimeMillis: Long, roomOffset: Long): Long {
        val state = calculate(currentTimeMillis, roomOffset)
        return state.remainingMillis
    }
}

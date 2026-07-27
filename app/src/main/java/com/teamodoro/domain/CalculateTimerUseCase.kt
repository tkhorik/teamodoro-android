package com.teamodoro.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The single source of truth for the shared Pomodoro cycle.
 *
 * The cycle is derived purely from the wall clock: `now % CYCLE_MILLIS` gives
 * the position within the current 30-minute cycle. No offset, no anchor, no
 * persisted state — two devices are synchronised iff their clocks are, which
 * is exactly how the reference web client behaves.
 *
 * Epoch millis are counted from 1970-01-01T00:00:00Z and `CYCLE_MILLIS`
 * divides an hour, so the modulo lands on the same half-hour boundaries as the
 * reference's local `getMinutes()` check for every timezone whose UTC offset is
 * a whole or half hour. (The handful of :45 zones — Nepal, Chatham Islands —
 * will be offset by 15 minutes from the website; this is intentional, see
 * README.)
 */
class CalculateTimerUseCase @Inject constructor() {

    /** Emits a fresh [TimerState] once per second, aligned to the second boundary. */
    fun timerStateFlow(): Flow<TimerState> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(now)
            // Align to the next whole second so the displayed countdown does not
            // lag behind the real transition by up to 999 ms.
            delay(1_000L - (now % 1_000L))
        }
    }.map { calculate(it) }

    /** Calculates the [TimerState] for a given wall-clock instant. */
    fun calculate(currentTimeMillis: Long): TimerState {
        val position = currentTimeMillis.mod(CYCLE_MILLIS)

        return if (position < WORK_DURATION_MILLIS) {
            TimerState(
                phase = TimerPhase.WORK,
                remainingMillis = WORK_DURATION_MILLIS - position,
                cyclePosition = position,
            )
        } else {
            TimerState(
                phase = TimerPhase.BREAK,
                remainingMillis = CYCLE_MILLIS - position,
                cyclePosition = position,
            )
        }
    }

    /** Millis from [currentTimeMillis] until the next phase transition. */
    fun millisUntilNextTransition(currentTimeMillis: Long): Long =
        calculate(currentTimeMillis).remainingMillis

    /** Absolute wall-clock instant of the next phase transition. Used to arm alarms. */
    fun nextTransitionAt(currentTimeMillis: Long): Long =
        currentTimeMillis + millisUntilNextTransition(currentTimeMillis)
}

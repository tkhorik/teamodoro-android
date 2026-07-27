package com.teamodoro.domain

/**
 * Immutable snapshot of the shared cycle at one instant.
 *
 * There is deliberately no `isRunning` flag here: the cycle is a property of
 * the clock, not of this app, so it is always running. Whether *this device*
 * is tracking it (foreground service + ongoing notification) is a separate
 * concern, held by `TimerPreferences`.
 */
data class TimerState(
    val phase: TimerPhase,
    val remainingMillis: Long,
    val cyclePosition: Long,
) {
    val progressFraction: Float
        get() {
            val phaseDuration = if (phase == TimerPhase.WORK) WORK_DURATION_MILLIS else BREAK_DURATION_MILLIS
            val elapsed = phaseDuration - remainingMillis
            return (elapsed.toFloat() / phaseDuration.toFloat()).coerceIn(0f, 1f)
        }

    companion object {
        val DEFAULT = TimerState(
            phase = TimerPhase.WORK,
            remainingMillis = WORK_DURATION_MILLIS,
            cyclePosition = 0L,
        )
    }
}

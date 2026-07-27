package com.teamodoro.domain

data class TimerState(
    val phase: TimerPhase,
    val remainingMillis: Long,
    val cyclePosition: Long,
    val isRunning: Boolean,
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
            isRunning = false,
        )
    }
}

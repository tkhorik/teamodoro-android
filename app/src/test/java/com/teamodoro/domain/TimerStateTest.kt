package com.teamodoro.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerStateTest {

    @Test
    fun `progress is derived from the active phase duration`() {
        assertEquals(
            0.5f,
            TimerState(TimerPhase.WORK, WORK_DURATION_MILLIS / 2, 0L).progressFraction,
            0.0001f,
        )
        assertEquals(
            0.5f,
            TimerState(TimerPhase.BREAK, BREAK_DURATION_MILLIS / 2, 0L).progressFraction,
            0.0001f,
        )
    }

    @Test
    fun `progress is clamped to a valid indicator range`() {
        assertEquals(0f, TimerState(TimerPhase.WORK, WORK_DURATION_MILLIS + 1, 0L).progressFraction, 0.0001f)
        assertEquals(1f, TimerState(TimerPhase.BREAK, 0L, 0L).progressFraction, 0.0001f)
    }
}

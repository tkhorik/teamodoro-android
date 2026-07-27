package com.teamodoro.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * The reference web client (https://github.com/BaseSecrete/teamodoro) defines the
 * cycle as a predicate over wall-clock minutes:
 *
 *     inBreak = (minutes >= 25 && minutes <= 29) || (minutes >= 55 && minutes <= 59)
 *
 * These tests pin our epoch-modulo implementation to that definition.
 */
class CalculateTimerUseCaseTest {

    private val useCase = CalculateTimerUseCase()

    /** Epoch millis for a UTC instant at [hour]:[minute]:[second] on 2026-01-01. */
    private fun utc(hour: Int, minute: Int, second: Int = 0, millis: Int = 0): Long {
        val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(2026, java.util.Calendar.JANUARY, 1, hour, minute, second)
        cal.set(java.util.Calendar.MILLISECOND, millis)
        return cal.timeInMillis
    }

    private fun minutes(n: Long) = TimeUnit.MINUTES.toMillis(n)

    // ---- Phase boundaries ------------------------------------------------

    @Test
    fun `top of the hour starts a full focus block`() {
        val state = useCase.calculate(utc(9, 0))
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(minutes(25), state.remainingMillis)
        assertEquals(0L, state.cyclePosition)
    }

    @Test
    fun `one millisecond before the break is still focus`() {
        val state = useCase.calculate(utc(9, 24, 59, 999))
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(1L, state.remainingMillis)
    }

    @Test
    fun `break starts exactly at 25 past`() {
        val state = useCase.calculate(utc(9, 25))
        assertEquals(TimerPhase.BREAK, state.phase)
        assertEquals(minutes(5), state.remainingMillis)
    }

    @Test
    fun `one millisecond before the half hour is still break`() {
        val state = useCase.calculate(utc(9, 29, 59, 999))
        assertEquals(TimerPhase.BREAK, state.phase)
        assertEquals(1L, state.remainingMillis)
    }

    @Test
    fun `half past starts the second focus block of the hour`() {
        val state = useCase.calculate(utc(9, 30))
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(minutes(25), state.remainingMillis)
        assertEquals(0L, state.cyclePosition)
    }

    @Test
    fun `break starts again at 55 past`() {
        val state = useCase.calculate(utc(9, 55))
        assertEquals(TimerPhase.BREAK, state.phase)
        assertEquals(minutes(5), state.remainingMillis)
    }

    // ---- Progress --------------------------------------------------------

    @Test
    fun `progress is zero at the start of each phase`() {
        assertEquals(0f, useCase.calculate(utc(9, 0)).progressFraction, 0.0001f)
        assertEquals(0f, useCase.calculate(utc(9, 25)).progressFraction, 0.0001f)
    }

    @Test
    fun `progress is half way through focus at 12 and a half minutes`() {
        val state = useCase.calculate(utc(9, 12, 30))
        assertEquals(0.5f, state.progressFraction, 0.0001f)
    }

    // ---- Transition scheduling ------------------------------------------

    @Test
    fun `next transition from mid-focus lands on the break boundary`() {
        val now = utc(9, 10)
        assertEquals(utc(9, 25), useCase.nextTransitionAt(now))
    }

    @Test
    fun `next transition from mid-break lands on the half hour`() {
        val now = utc(9, 27)
        assertEquals(utc(9, 30), useCase.nextTransitionAt(now))
    }

    @Test
    fun `next transition exactly on a boundary is a full phase away`() {
        assertEquals(utc(9, 25), useCase.nextTransitionAt(utc(9, 0)))
    }

    // ---- Cross-check against the reference predicate ---------------------

    @Test
    fun `phase matches the reference JS predicate for every minute of the day`() {
        for (minuteOfDay in 0 until 24 * 60) {
            val hour = minuteOfDay / 60
            val minute = minuteOfDay % 60
            val expectedBreak = (minute in 25..29) || (minute in 55..59)
            val actual = useCase.calculate(utc(hour, minute)).phase
            val expected = if (expectedBreak) TimerPhase.BREAK else TimerPhase.WORK
            assertEquals("mismatch at %02d:%02d".format(hour, minute), expected, actual)
        }
    }

    @Test
    fun `remaining time never exceeds the phase duration`() {
        for (secondOfCycle in 0 until 30 * 60) {
            val state = useCase.calculate(utc(9, 0) + secondOfCycle * 1_000L)
            val max = if (state.phase == TimerPhase.WORK) WORK_DURATION_MILLIS else BREAK_DURATION_MILLIS
            assert(state.remainingMillis in 1..max) {
                "remaining ${state.remainingMillis} out of range at second $secondOfCycle"
            }
        }
    }

    // ---- Robustness ------------------------------------------------------

    @Test
    fun `pre-epoch timestamps do not produce a negative cycle position`() {
        // Long.mod keeps the result non-negative; a plain % would not.
        val state = useCase.calculate(-1L)
        assert(state.cyclePosition >= 0L) { "negative cycle position: ${state.cyclePosition}" }
        assertEquals(CYCLE_MILLIS - 1L, state.cyclePosition)
    }

    @Test
    fun `two clients one cycle apart see the same phase and remaining time`() {
        val a = useCase.calculate(utc(9, 7, 42))
        val b = useCase.calculate(utc(9, 37, 42))
        assertEquals(a.phase, b.phase)
        assertEquals(a.remainingMillis, b.remainingMillis)
        assertEquals(a.cyclePosition, b.cyclePosition)
    }
}

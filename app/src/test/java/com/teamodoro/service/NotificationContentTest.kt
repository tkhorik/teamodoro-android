package com.teamodoro.service

import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationContentTest {

    @Test
    fun `timer notification describes focus time remaining`() {
        val content = NotificationContent.timer(
            TimerState(TimerPhase.WORK, remainingMillis = 12 * 60_000L + 34_000L, cyclePosition = 0L),
        )

        assertEquals("Focus — 12:34 remaining", content.title)
        assertEquals("Teamodoro is running", content.text)
    }

    @Test
    fun `timer notification describes break time remaining`() {
        val content = NotificationContent.timer(
            TimerState(TimerPhase.BREAK, remainingMillis = 59_000L, cyclePosition = 0L),
        )

        assertEquals("Break — 00:59 remaining", content.title)
    }

    @Test
    fun `transition notifications use the expected focus and break copy`() {
        assertEquals(
            Content("Focus time!", "25-minute focus session started"),
            NotificationContent.transition(TimerPhase.WORK),
        )
        assertEquals(
            Content("Take a break!", "5-minute break started"),
            NotificationContent.transition(TimerPhase.BREAK),
        )
    }
}

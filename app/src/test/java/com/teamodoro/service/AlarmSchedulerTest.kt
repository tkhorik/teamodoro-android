package com.teamodoro.service

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSchedulerTest {

    @Test
    fun `transition alarm has one canonical pending intent identity`() {
        val identity = AlarmScheduler.transitionRequestIdentity

        assertEquals(AlarmReceiver::class.java.name, identity.receiverClassName)
        assertEquals(AlarmScheduler.ACTION_PHASE_TRANSITION, identity.action)
        assertEquals(1001, identity.requestCode)
    }

    @Test
    fun `scheduled transition action is forwarded to the timer service`() {
        assertEquals(AlarmScheduler.ACTION_PHASE_TRANSITION, TimerService.ACTION_PHASE_TRANSITION)
    }
}

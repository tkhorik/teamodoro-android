package com.teamodoro.service

import android.app.NotificationManager
import android.media.AudioManager
import com.teamodoro.domain.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TickSoundPolicyTest {

    private fun shouldTick(
        enabled: Boolean = true,
        isTracking: Boolean = true,
        phase: TimerPhase = TimerPhase.WORK,
        interruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL,
        ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    ) = TickSoundPolicy.shouldTick(enabled, isTracking, phase, interruptionFilter, ringerMode)

    @Test
    fun `only enabled tracking focus ticks`() {
        for (enabled in listOf(true, false)) {
            for (isTracking in listOf(true, false)) {
                for (phase in TimerPhase.values()) {
                    val expected = enabled && isTracking && phase == TimerPhase.WORK
                    assertEquals(
                        "enabled=$enabled isTracking=$isTracking phase=$phase",
                        expected,
                        shouldTick(enabled = enabled, isTracking = isTracking, phase = phase),
                    )
                }
            }
        }
    }

    @Test
    fun `do not disturb blocks the tick`() {
        assertFalse(shouldTick(interruptionFilter = NotificationManager.INTERRUPTION_FILTER_PRIORITY))
        assertFalse(shouldTick(interruptionFilter = NotificationManager.INTERRUPTION_FILTER_NONE))
    }

    @Test
    fun `vibrate and silent ringer modes block the tick`() {
        assertFalse(shouldTick(ringerMode = AudioManager.RINGER_MODE_VIBRATE))
        assertFalse(shouldTick(ringerMode = AudioManager.RINGER_MODE_SILENT))
    }
}

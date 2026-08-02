package com.teamodoro.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingPolicyTest {

    @Test
    fun `enabled tracking accepts a phase transition`() {
        assertTrue(
            TrackingPolicy.shouldHandlePhaseTransition(
                AlarmScheduler.ACTION_PHASE_TRANSITION,
                isTracking = true,
            ),
        )
    }

    @Test
    fun `stopped tracking rejects a stale phase transition`() {
        assertFalse(
            TrackingPolicy.shouldHandlePhaseTransition(
                AlarmScheduler.ACTION_PHASE_TRANSITION,
                isTracking = false,
            ),
        )
    }

    @Test
    fun `unrelated broadcasts never start transition handling`() {
        assertFalse(TrackingPolicy.shouldHandlePhaseTransition("unexpected.action", isTracking = true))
    }

    @Test
    fun `boot restores only enabled tracking`() {
        assertTrue(TrackingPolicy.shouldRestoreAfterBoot(TrackingPolicy.ACTION_BOOT_COMPLETED, isTracking = true))
        assertFalse(TrackingPolicy.shouldRestoreAfterBoot(TrackingPolicy.ACTION_BOOT_COMPLETED, isTracking = false))
        assertFalse(TrackingPolicy.shouldRestoreAfterBoot("unexpected.action", isTracking = true))
    }
}

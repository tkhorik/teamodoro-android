package com.teamodoro.service

import android.app.NotificationManager
import android.media.AudioManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransitionSoundPolicyTest {

    private fun allowed(
        notificationsEnabled: Boolean = true,
        channelImportance: Int = NotificationManager.IMPORTANCE_HIGH,
        interruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL,
        ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    ) = TransitionSoundPolicy.isPlaybackAllowed(
        notificationsEnabled,
        channelImportance,
        interruptionFilter,
        ringerMode,
    )

    @Test
    fun `all clear allows playback`() {
        assertTrue(allowed())
    }

    @Test
    fun `a channel at default importance still allows playback`() {
        assertTrue(allowed(channelImportance = NotificationManager.IMPORTANCE_DEFAULT))
    }

    @Test
    fun `disabled notifications block playback`() {
        assertFalse(allowed(notificationsEnabled = false))
    }

    @Test
    fun `a channel below default importance blocks playback`() {
        assertFalse(allowed(channelImportance = NotificationManager.IMPORTANCE_LOW))
        assertFalse(allowed(channelImportance = NotificationManager.IMPORTANCE_NONE))
    }

    @Test
    fun `do not disturb blocks playback`() {
        assertFalse(allowed(interruptionFilter = NotificationManager.INTERRUPTION_FILTER_PRIORITY))
        assertFalse(allowed(interruptionFilter = NotificationManager.INTERRUPTION_FILTER_NONE))
    }

    @Test
    fun `vibrate and silent ringer modes block playback`() {
        assertFalse(allowed(ringerMode = AudioManager.RINGER_MODE_VIBRATE))
        assertFalse(allowed(ringerMode = AudioManager.RINGER_MODE_SILENT))
    }
}

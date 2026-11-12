package com.teamodoro.service

import android.app.NotificationManager
import android.media.AudioManager
import com.teamodoro.domain.TimerPhase

/**
 * Pure rules for deciding whether the quiet focus tick sounds this second.
 *
 * The tick only plays during focus while tracking is on, and stays silent under
 * Do Not Disturb and on vibrate or silent, like [TransitionSoundPolicy].
 */
internal object TickSoundPolicy {

    fun shouldTick(
        enabled: Boolean,
        isTracking: Boolean,
        phase: TimerPhase,
        interruptionFilter: Int,
        ringerMode: Int,
    ): Boolean = enabled &&
        isTracking &&
        phase == TimerPhase.WORK &&
        interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL &&
        ringerMode == AudioManager.RINGER_MODE_NORMAL
}

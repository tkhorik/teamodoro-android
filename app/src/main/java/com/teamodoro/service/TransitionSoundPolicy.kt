package com.teamodoro.service

import android.app.NotificationManager
import android.media.AudioManager

/**
 * Pure rules for deciding whether the app may play a transition sound itself.
 *
 * The app plays the sound as notification audio instead of through the channel,
 * so the platform no longer silences it for us. These checks keep it quiet when
 * the user has blocked or lowered the alerts, turned on Do Not Disturb, or put
 * the phone on vibrate or silent.
 */
internal object TransitionSoundPolicy {

    fun isPlaybackAllowed(
        notificationsEnabled: Boolean,
        channelImportance: Int,
        interruptionFilter: Int,
        ringerMode: Int,
    ): Boolean = notificationsEnabled &&
        channelImportance >= NotificationManager.IMPORTANCE_DEFAULT &&
        interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL &&
        ringerMode == AudioManager.RINGER_MODE_NORMAL
}

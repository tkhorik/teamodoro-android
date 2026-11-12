package com.teamodoro.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.teamodoro.data.CustomSoundResolver
import com.teamodoro.data.PlaybackTarget
import com.teamodoro.data.SoundEvent
import com.teamodoro.data.SoundPreferences
import com.teamodoro.data.TransitionSoundRules
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the user's chosen sound for a phase boundary.
 *
 * The transition channel is deliberately silent (a channel's sound can't be
 * changed once it exists), so the sound is played here, as notification audio.
 */
@Singleton
class TransitionSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val soundPreferences: SoundPreferences,
    private val customSoundResolver: CustomSoundResolver,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var current: Ringtone? = null

    /** Call after the transition notification has been posted, so its channel exists. */
    fun play(event: SoundEvent) {
        try {
            if (!isPlaybackAllowed()) return
            val target = TransitionSoundRules.playbackTarget(
                soundPreferences.get(event),
                customSoundResolver::isAvailable,
            )
            val uri = when (target) {
                PlaybackTarget.None -> return
                PlaybackTarget.DefaultSound -> Settings.System.DEFAULT_NOTIFICATION_URI
                is PlaybackTarget.Uri -> Uri.parse(target.uri)
            }

            current?.stop()
            current = RingtoneManager.getRingtone(context, uri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not play the transition sound", e)
        }
    }

    private fun isPlaybackAllowed(): Boolean = TransitionSoundPolicy.isPlaybackAllowed(
        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
        channelImportance = notificationManager
            .getNotificationChannel(NotificationHelper.CHANNEL_TRANSITION)
            ?.importance
            ?: NotificationManager.IMPORTANCE_NONE,
        interruptionFilter = notificationManager.currentInterruptionFilter,
        ringerMode = audioManager.ringerMode,
    )

    private companion object {
        const val TAG = "TransitionSoundPlayer"
    }
}

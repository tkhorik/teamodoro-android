package com.teamodoro.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the quiet focus tick: one very short, low-volume tone per call.
 *
 * It uses the platform [ToneGenerator] on the notification stream and never
 * requests audio focus, so music playing in another app is neither paused nor
 * ducked. Whether a tick is due is decided by [TickSoundPolicy].
 */
@Singleton
class TickSoundPlayer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private var toneGenerator: ToneGenerator? = null

    fun tick() {
        try {
            val generator = toneGenerator
                ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, VOLUME).also { toneGenerator = it }
            generator.startTone(ToneGenerator.TONE_PROP_BEEP, DURATION_MILLIS)
        } catch (e: Exception) {
            Log.w(TAG, "Could not play the focus tick", e)
        }
    }

    /** Frees the generator. The next [tick] creates a new one. */
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }

    private companion object {
        const val TAG = "TickSoundPlayer"
        const val VOLUME = 20
        const val DURATION_MILLIS = 40
    }
}

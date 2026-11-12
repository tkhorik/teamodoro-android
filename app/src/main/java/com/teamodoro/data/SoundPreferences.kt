package com.teamodoro.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Both sound choices at one moment. */
data class SoundChoices(
    val focusStart: TransitionSound,
    val focusEnd: TransitionSound,
) {
    operator fun get(event: SoundEvent): TransitionSound = when (event) {
        SoundEvent.FOCUS_START -> focusStart
        SoundEvent.FOCUS_END -> focusEnd
    }
}

/**
 * The sound the user picked for each phase boundary.
 *
 * SharedPreferences, like [TimerPreferences]: the service reads the choice
 * synchronously while posting a notification, which DataStore can't do.
 */
@Singleton
class SoundPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(event: SoundEvent): TransitionSound =
        TransitionSoundRules.decode(prefs.getString(keyFor(event), null))

    fun set(event: SoundEvent, sound: TransitionSound) {
        prefs.edit().putString(keyFor(event), TransitionSoundRules.encode(sound)).apply()
    }

    /** Emits the current choices immediately, then on every change. */
    val choicesFlow: Flow<SoundChoices> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_FOCUS_START || key == KEY_FOCUS_END) trySend(currentChoices())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(currentChoices())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun currentChoices() = SoundChoices(
        focusStart = get(SoundEvent.FOCUS_START),
        focusEnd = get(SoundEvent.FOCUS_END),
    )

    private fun keyFor(event: SoundEvent): String = when (event) {
        SoundEvent.FOCUS_START -> KEY_FOCUS_START
        SoundEvent.FOCUS_END -> KEY_FOCUS_END
    }

    private companion object {
        const val PREFS_NAME = "teamodoro_sound_prefs"
        const val KEY_FOCUS_START = "focus_start_sound"
        const val KEY_FOCUS_END = "focus_end_sound"
    }
}

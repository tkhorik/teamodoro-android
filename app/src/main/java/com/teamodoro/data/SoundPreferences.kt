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
 * The sound the user picked for each phase boundary, and whether the quiet tick
 * plays every second during focus (off by default).
 *
 * SharedPreferences, like [TimerPreferences]: the service reads these
 * synchronously while posting a notification or on each tick, which DataStore
 * can't do.
 */
@Singleton
class SoundPreferences internal constructor(
    private val prefs: SharedPreferences,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    )

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

    /** Whether the focus tick is on. Read fresh each second by the service. */
    fun isFocusTickEnabled(): Boolean = prefs.getBoolean(KEY_FOCUS_TICK, false)

    fun setFocusTickEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_TICK, enabled).apply()
    }

    /** Emits the current focus-tick setting immediately, then on every change. */
    val focusTickEnabledFlow: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_FOCUS_TICK) trySend(isFocusTickEnabled())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(isFocusTickEnabled())
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
        const val KEY_FOCUS_TICK = "focus_tick_enabled"
    }
}

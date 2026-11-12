package com.teamodoro.data

import com.teamodoro.domain.TimerPhase

/** What the user chose to hear at a phase boundary. */
sealed interface TransitionSound {
    /** The system default notification sound. */
    data object Default : TransitionSound

    data object Silent : TransitionSound

    /** A sound picked from the system picker; [uri] is always a `content://` URI. */
    data class Custom(val uri: String) : TransitionSound
}

/** A phase boundary a sound can be chosen for. */
enum class SoundEvent {
    FOCUS_START,
    FOCUS_END,
    ;

    companion object {
        /** The event announced when the timer enters [phase]. */
        fun forPhase(phase: TimerPhase): SoundEvent = when (phase) {
            TimerPhase.WORK -> FOCUS_START
            TimerPhase.BREAK -> FOCUS_END
        }
    }
}

/** What [TransitionSoundRules.playbackTarget] decided to play. */
sealed interface PlaybackTarget {
    data object None : PlaybackTarget

    data object DefaultSound : PlaybackTarget

    data class Uri(val uri: String) : PlaybackTarget
}

/**
 * Pure rules for storing, choosing and playing transition sounds. No Android
 * imports, so they run in plain JVM tests.
 *
 * Only `content://` URIs are accepted: the system picker returns nothing else,
 * and a stored value is untrusted input when it is read back.
 */
object TransitionSoundRules {

    private const val STORED_DEFAULT = "default"
    private const val STORED_SILENT = "silent"
    private const val STORED_CUSTOM_PREFIX = "custom:"
    private const val CONTENT_SCHEME_PREFIX = "content://"

    fun encode(sound: TransitionSound): String = when (sound) {
        TransitionSound.Default -> STORED_DEFAULT
        TransitionSound.Silent -> STORED_SILENT
        is TransitionSound.Custom -> STORED_CUSTOM_PREFIX + sound.uri
    }

    /** Anything missing, blank, unrecognised or not a `content://` URI is [TransitionSound.Default]. */
    fun decode(stored: String?): TransitionSound = when {
        stored == STORED_SILENT -> TransitionSound.Silent
        stored != null && stored.startsWith(STORED_CUSTOM_PREFIX) ->
            stored.removePrefix(STORED_CUSTOM_PREFIX).let { uri ->
                if (isContentUri(uri)) TransitionSound.Custom(uri) else TransitionSound.Default
            }
        else -> TransitionSound.Default
    }

    /** Maps the picker's result: a null URI means "Silent" was chosen. */
    fun fromPickerResult(pickedUri: String?, defaultUri: String): TransitionSound = when {
        pickedUri == null -> TransitionSound.Silent
        pickedUri == defaultUri -> TransitionSound.Default
        isContentUri(pickedUri) -> TransitionSound.Custom(pickedUri)
        else -> TransitionSound.Default
    }

    /** A custom sound that can no longer be opened falls back to the default one. */
    fun playbackTarget(sound: TransitionSound, isAvailable: (String) -> Boolean): PlaybackTarget =
        when (sound) {
            TransitionSound.Silent -> PlaybackTarget.None
            TransitionSound.Default -> PlaybackTarget.DefaultSound
            is TransitionSound.Custom ->
                if (isAvailable(sound.uri)) PlaybackTarget.Uri(sound.uri) else PlaybackTarget.DefaultSound
        }

    private fun isContentUri(value: String): Boolean =
        value.length > CONTENT_SCHEME_PREFIX.length &&
            value.startsWith(CONTENT_SCHEME_PREFIX, ignoreCase = true)
}

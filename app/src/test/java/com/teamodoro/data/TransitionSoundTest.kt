package com.teamodoro.data

import com.teamodoro.domain.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class TransitionSoundTest {

    private val defaultUri = "content://settings/system/notification_sound"
    private val customUri = "content://media/internal/audio/media/42"

    @Test
    fun `every variant survives an encode and decode round trip`() {
        listOf(
            TransitionSound.Default,
            TransitionSound.Silent,
            TransitionSound.Custom(customUri),
        ).forEach { sound ->
            assertEquals(sound, TransitionSoundRules.decode(TransitionSoundRules.encode(sound)))
        }
    }

    @Test
    fun `missing blank and garbage values decode to default`() {
        listOf(null, "", "   ", "garbage", "custom", "DEFAULT").forEach { stored ->
            assertEquals(TransitionSound.Default, TransitionSoundRules.decode(stored))
        }
    }

    @Test
    fun `custom values without a content scheme decode to default`() {
        listOf(
            "custom:file:///sdcard/ring.mp3",
            "custom:http://example.com/ring.mp3",
            "custom:/sdcard/ring.mp3",
            "custom:content://",
            "custom:",
        ).forEach { stored ->
            assertEquals(TransitionSound.Default, TransitionSoundRules.decode(stored))
        }
    }

    @Test
    fun `a null picker result means silent`() {
        assertEquals(TransitionSound.Silent, TransitionSoundRules.fromPickerResult(null, defaultUri))
    }

    @Test
    fun `the default uri from the picker means default`() {
        assertEquals(TransitionSound.Default, TransitionSoundRules.fromPickerResult(defaultUri, defaultUri))
    }

    @Test
    fun `a content uri from the picker is a custom sound`() {
        assertEquals(
            TransitionSound.Custom(customUri),
            TransitionSoundRules.fromPickerResult(customUri, defaultUri),
        )
    }

    @Test
    fun `non content picker results mean default`() {
        listOf("file:///sdcard/ring.mp3", "http://example.com/ring.mp3", "garbage", "").forEach { picked ->
            assertEquals(TransitionSound.Default, TransitionSoundRules.fromPickerResult(picked, defaultUri))
        }
    }

    @Test
    fun `focus start is the work phase and focus end is the break phase`() {
        assertEquals(SoundEvent.FOCUS_START, SoundEvent.forPhase(TimerPhase.WORK))
        assertEquals(SoundEvent.FOCUS_END, SoundEvent.forPhase(TimerPhase.BREAK))
    }

    @Test
    fun `an available custom sound plays its uri`() {
        assertEquals(
            PlaybackTarget.Uri(customUri),
            TransitionSoundRules.playbackTarget(TransitionSound.Custom(customUri)) { true },
        )
    }

    @Test
    fun `an unavailable custom sound falls back to the default sound`() {
        assertEquals(
            PlaybackTarget.DefaultSound,
            TransitionSoundRules.playbackTarget(TransitionSound.Custom(customUri)) { false },
        )
    }

    @Test
    fun `default plays the default sound without checking availability`() {
        assertEquals(
            PlaybackTarget.DefaultSound,
            TransitionSoundRules.playbackTarget(TransitionSound.Default) { error("not asked") },
        )
    }

    @Test
    fun `silent plays nothing`() {
        assertEquals(
            PlaybackTarget.None,
            TransitionSoundRules.playbackTarget(TransitionSound.Silent) { true },
        )
    }
}

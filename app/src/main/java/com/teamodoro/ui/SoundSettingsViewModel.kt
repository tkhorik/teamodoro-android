package com.teamodoro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamodoro.data.CustomSoundResolver
import com.teamodoro.data.SoundEvent
import com.teamodoro.data.SoundPreferences
import com.teamodoro.data.TransitionSound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * What the dialog shows for one event. [sound] is what will actually play: a
 * stored custom sound that can't be opened any more shows up as
 * [TransitionSound.Default]. [customTitle] is set only for [TransitionSound.Custom].
 */
data class SoundChoiceUi(
    val sound: TransitionSound = TransitionSound.Default,
    val customTitle: String? = null,
)

data class SoundSettingsState(
    val focusStart: SoundChoiceUi = SoundChoiceUi(),
    val focusEnd: SoundChoiceUi = SoundChoiceUi(),
) {
    operator fun get(event: SoundEvent): SoundChoiceUi = when (event) {
        SoundEvent.FOCUS_START -> focusStart
        SoundEvent.FOCUS_END -> focusEnd
    }
}

@HiltViewModel
class SoundSettingsViewModel @Inject constructor(
    private val soundPreferences: SoundPreferences,
    private val customSoundResolver: CustomSoundResolver,
) : ViewModel() {

    val state: StateFlow<SoundSettingsState> = soundPreferences.choicesFlow
        .map { choices ->
            SoundSettingsState(
                focusStart = resolve(choices.focusStart),
                focusEnd = resolve(choices.focusEnd),
            )
        }
        // Checking and naming a custom sound queries the system.
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SoundSettingsState(),
        )

    fun setSound(event: SoundEvent, sound: TransitionSound) {
        soundPreferences.set(event, sound)
    }

    private fun resolve(sound: TransitionSound): SoundChoiceUi = when (sound) {
        TransitionSound.Default, TransitionSound.Silent -> SoundChoiceUi(sound)
        is TransitionSound.Custom -> {
            val title = if (customSoundResolver.isAvailable(sound.uri)) {
                customSoundResolver.title(sound.uri)
            } else {
                null
            }
            if (title != null) SoundChoiceUi(sound, title) else SoundChoiceUi(TransitionSound.Default)
        }
    }
}

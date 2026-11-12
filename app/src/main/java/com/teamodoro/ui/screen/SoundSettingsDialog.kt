package com.teamodoro.ui.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import com.teamodoro.R
import com.teamodoro.data.SoundEvent
import com.teamodoro.data.TransitionSound
import com.teamodoro.data.TransitionSoundRules
import com.teamodoro.ui.SoundChoiceUi
import com.teamodoro.ui.SoundSettingsState

/**
 * Lets the user pick the sound for focus start and focus end through the
 * system notification-sound picker, which offers "Default" and "Silent" itself.
 */
@Composable
fun SoundSettingsDialog(
    state: SoundSettingsState,
    onSoundSelected: (SoundEvent, TransitionSound) -> Unit,
    onDismiss: () -> Unit,
) {
    // Saved, so the row being edited survives the picker recreating the activity.
    var pendingEvent by rememberSaveable { mutableStateOf<SoundEvent?>(null) }
    val defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val event = pendingEvent ?: return@rememberLauncherForActivityResult
        pendingEvent = null
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val picked = result.data?.let {
            IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        }
        onSoundSelected(
            event,
            TransitionSoundRules.fromPickerResult(picked?.toString(), defaultUri.toString()),
        )
    }

    fun launchPicker(event: SoundEvent, title: String) {
        val existingUri = when (val sound = state[event].sound) {
            TransitionSound.Silent -> null
            TransitionSound.Default -> defaultUri
            is TransitionSound.Custom -> Uri.parse(sound.uri)
        }
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
        pendingEvent = event
        try {
            pickerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            pendingEvent = null
        }
    }

    val focusStartLabel = stringResource(R.string.sound_focus_start)
    val focusEndLabel = stringResource(R.string.sound_focus_end)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sound_settings_title)) },
        text = {
            Column {
                SoundRow(
                    label = focusStartLabel,
                    choice = state.focusStart,
                    onClick = { launchPicker(SoundEvent.FOCUS_START, focusStartLabel) },
                )
                SoundRow(
                    label = focusEndLabel,
                    choice = state.focusEnd,
                    onClick = { launchPicker(SoundEvent.FOCUS_END, focusEndLabel) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

@Composable
private fun SoundRow(
    label: String,
    choice: SoundChoiceUi,
    onClick: () -> Unit,
) {
    val choiceName = when (choice.sound) {
        TransitionSound.Default -> stringResource(R.string.sound_default)
        TransitionSound.Silent -> stringResource(R.string.sound_silent)
        is TransitionSound.Custom -> choice.customTitle ?: stringResource(R.string.sound_default)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = choiceName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

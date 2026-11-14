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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import com.teamodoro.R
import com.teamodoro.data.SoundEvent
import com.teamodoro.data.TransitionSound
import com.teamodoro.data.TransitionSoundRules
import com.teamodoro.locale.LocaleManager
import com.teamodoro.ui.SoundChoiceUi
import com.teamodoro.ui.SoundSettingsState
import java.util.Locale

/** Settings for transition sounds and the app language. */
@Composable
fun SettingsDialog(
    state: SoundSettingsState,
    currentLanguageTag: String?,
    supportedLanguages: List<LocaleManager.AppLanguage>,
    onSoundSelected: (SoundEvent, TransitionSound) -> Unit,
    onLanguageClick: () -> Unit,
    onDismiss: () -> Unit,
) {
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
    val languageLabel = stringResource(R.string.settings_language_label)
    val languageValue = resolveLanguageLabel(currentLanguageTag, supportedLanguages)
        ?: stringResource(R.string.settings_language_system_default)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.sound_settings_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button, onClick = onLanguageClick)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "$languageLabel: $languageValue"
                        }
                        .padding(vertical = 12.dp),
                ) {
                    Text(
                        text = languageLabel,
                        modifier = Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = languageValue,
                        modifier = Modifier.clearAndSetSemantics {},
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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

internal fun resolveLanguageLabel(
    currentLanguageTag: String?,
    supportedLanguages: List<LocaleManager.AppLanguage>,
): String? = supportedLanguages
    .firstOrNull { it.tag == currentLanguageTag }
    ?.displayName
    ?: currentLanguageTag?.let { Locale.forLanguageTag(it).getDisplayName(Locale.forLanguageTag(it)) }

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 320)
@Composable
private fun SettingsUkrainianLanguagePreview() {
    com.teamodoro.ui.theme.AppTheme(
        phase = com.teamodoro.domain.TimerPhase.WORK,
        themeMode = com.teamodoro.domain.ThemeMode.LIGHT,
    ) {
        SettingsDialog(
            state = SoundSettingsState(),
            currentLanguageTag = "uk",
            supportedLanguages = listOf(LocaleManager.AppLanguage(tag = "uk", displayName = "Українська")),
            onSoundSelected = { _, _ -> },
            onLanguageClick = {},
            onDismiss = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 320, locale = "es")
@Composable
private fun SettingsSpanishSystemDefaultPreview() {
    com.teamodoro.ui.theme.AppTheme(
        phase = com.teamodoro.domain.TimerPhase.WORK,
        themeMode = com.teamodoro.domain.ThemeMode.LIGHT,
    ) {
        SettingsDialog(
            state = SoundSettingsState(),
            currentLanguageTag = null,
            supportedLanguages = emptyList(),
            onSoundSelected = { _, _ -> },
            onLanguageClick = {},
            onDismiss = {},
        )
    }
}

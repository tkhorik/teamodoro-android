package com.teamodoro.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teamodoro.R
import com.teamodoro.locale.LocaleManager

/**
 * In-app language override picker for API 26-32, which have no system Settings
 * screen of their own for this (see [com.teamodoro.locale.LocaleManager] and
 * MainActivity for where this is only shown below API 33).
 */
@Composable
fun LanguagePickerDialog(
    languages: List<LocaleManager.AppLanguage>,
    currentTag: String?,
    onLanguageSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column {
                languages.forEach { language ->
                    val selected = language.tag == currentTag
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { onLanguageSelected(language.tag) },
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioRow(
                            label = language.displayName
                                ?: stringResource(R.string.settings_language_system_default),
                            selected = selected,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

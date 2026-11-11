package com.teamodoro.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teamodoro.R

/**
 * Short guide to the clock-aligned cycle, opened by tapping the app title in
 * [TimerScreen].
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.about_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(
                        R.string.about_dialog_cycle,
                        AboutGuide.focusMinutes,
                        AboutGuide.breakMinutes,
                        AboutGuide.cycleMinutes,
                    ),
                )
                Text(stringResource(R.string.about_dialog_clock))
                Text(stringResource(R.string.about_dialog_notifications))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.about_dialog_confirm))
            }
        },
    )
}

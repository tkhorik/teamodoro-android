package com.teamodoro.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamodoro.R
import com.teamodoro.data.SoundEvent
import com.teamodoro.data.TransitionSound
import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
import com.teamodoro.domain.ThemeMode
import com.teamodoro.locale.LocaleManager
import com.teamodoro.ui.SoundSettingsState
import com.teamodoro.ui.theme.AppTheme
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimerScreen(
    state: TimerState,
    isTracking: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    currentLanguageTag: String?,
    supportedLanguages: List<LocaleManager.AppLanguage>,
    onLanguageRowClick: () -> Unit,
    themeMode: ThemeMode,
    onThemeToggle: () -> Unit,
    soundSettings: SoundSettingsState,
    onSoundSelected: (SoundEvent, TransitionSound) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showSoundSettings by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.about_open_label),
                            role = Role.Button,
                            onClick = { showAbout = true },
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = { showSoundSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = stringResource(R.string.sound_settings_open),
                        )
                    }
                    ThemeToggleButton(
                        themeMode = themeMode,
                        onClick = onThemeToggle,
                    )
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PhaseLabel(phase = state.phase)

                Spacer(modifier = Modifier.height(40.dp))

                TimerRing(state = state)

                Spacer(modifier = Modifier.height(48.dp))

                ControlButtons(
                    isTracking = isTracking,
                    onStart = onStart,
                    onStop = onStop,
                )

                Spacer(modifier = Modifier.height(24.dp))

                LanguagePill(
                    currentLanguageTag = currentLanguageTag,
                    supportedLanguages = supportedLanguages,
                    onClick = onLanguageRowClick,
                )
            }
        }
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    if (showSoundSettings) {
        SoundSettingsDialog(
            state = soundSettings,
            onSoundSelected = onSoundSelected,
            onDismiss = { showSoundSettings = false },
        )
    }
}

@Composable
private fun ThemeToggleButton(
    themeMode: ThemeMode,
    onClick: () -> Unit,
) {
    val systemIsDark = androidx.compose.foundation.isSystemInDarkTheme()
    val activeDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemIsDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val contentDescription = stringResource(
        if (activeDarkTheme) R.string.theme_switch_to_light else R.string.theme_switch_to_dark,
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (activeDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun PhaseLabel(phase: TimerPhase) {
    val labelRes = if (phase == TimerPhase.WORK) R.string.phase_focus else R.string.phase_break
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun TimerRing(state: TimerState) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progressFraction,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress",
    )

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(220.dp),
            strokeWidth = 12.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.remainingMillis.toMinutesSeconds(),
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.timer_remaining),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun ControlButtons(
    isTracking: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    // The cycle runs whether or not this device is watching it, so these
    // buttons control the notifications and the tile, not the timer itself.
    if (isTracking) {
        OutlinedButton(onClick = onStop) {
            Text(stringResource(R.string.btn_notifications_stop))
        }
    } else {
        Button(onClick = onStart) {
            Text(stringResource(R.string.btn_notifications_start))
        }
    }
}

@Composable
private fun LanguagePill(
    currentLanguageTag: String?,
    supportedLanguages: List<LocaleManager.AppLanguage>,
    onClick: () -> Unit,
) {
    val label = resolveLanguageLabel(currentLanguageTag, supportedLanguages)
        ?: stringResource(R.string.settings_language_system_default)
    val description = "${stringResource(R.string.settings_language_label)}: $label"
    val colorScheme = MaterialTheme.colorScheme

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                // The chip's content description already announces the label.
                modifier = Modifier.clearAndSetSemantics {},
                maxLines = 1,
                softWrap = false,
            )
        },
        modifier = Modifier.semantics {
            contentDescription = description
            role = Role.Button
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = colorScheme.onSurface,
            leadingIconContentColor = colorScheme.onSurface,
            trailingIconContentColor = colorScheme.onSurface,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = colorScheme.onSurface.copy(alpha = 0.38f),
        ),
    )
}

/**
 * The current language's name in its own language, or null when the app follows
 * the system language and the caller should show the "System default" string.
 */
internal fun resolveLanguageLabel(
    currentLanguageTag: String?,
    supportedLanguages: List<LocaleManager.AppLanguage>,
): String? = supportedLanguages
    .firstOrNull { it.tag == currentLanguageTag }
    ?.displayName
    ?: currentLanguageTag?.let { Locale.forLanguageTag(it).getDisplayName(Locale.forLanguageTag(it)) }

private fun Long.toMinutesSeconds(): String {
    val totalSeconds = this / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenWorkPreview() {
    AppTheme(phase = TimerPhase.WORK, themeMode = ThemeMode.LIGHT) {
        TimerScreen(
            state = TimerState(
                phase = TimerPhase.WORK,
                remainingMillis = 17 * 60 * 1000L,
                cyclePosition = 8 * 60 * 1000L,
            ),
            isTracking = true,
            onStart = {},
            onStop = {},
            currentLanguageTag = null,
            supportedLanguages = emptyList(),
            onLanguageRowClick = {},
            themeMode = ThemeMode.LIGHT,
            onThemeToggle = {},
            soundSettings = SoundSettingsState(),
            onSoundSelected = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenBreakPreview() {
    AppTheme(phase = TimerPhase.BREAK, themeMode = ThemeMode.DARK) {
        TimerScreen(
            state = TimerState(
                phase = TimerPhase.BREAK,
                remainingMillis = 3 * 60 * 1000L,
                cyclePosition = 27 * 60 * 1000L,
            ),
            isTracking = false,
            onStart = {},
            onStop = {},
            currentLanguageTag = null,
            supportedLanguages = emptyList(),
            onLanguageRowClick = {},
            themeMode = ThemeMode.DARK,
            onThemeToggle = {},
            soundSettings = SoundSettingsState(),
            onSoundSelected = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun TimerScreenNarrowLongLabelPreview() {
    AppTheme(phase = TimerPhase.WORK, themeMode = ThemeMode.LIGHT) {
        TimerScreen(
            state = TimerState(
                phase = TimerPhase.WORK,
                remainingMillis = 17 * 60 * 1000L,
                cyclePosition = 8 * 60 * 1000L,
            ),
            isTracking = false,
            onStart = {},
            onStop = {},
            currentLanguageTag = "uk",
            supportedLanguages = listOf(
                LocaleManager.AppLanguage(tag = null, displayName = null),
                LocaleManager.AppLanguage(tag = "en", displayName = "English"),
                LocaleManager.AppLanguage(tag = "uk", displayName = "Українська"),
            ),
            onLanguageRowClick = {},
            themeMode = ThemeMode.LIGHT,
            onThemeToggle = {},
            soundSettings = SoundSettingsState(),
            onSoundSelected = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, widthDp = 320, locale = "es")
@Composable
private fun TimerScreenNarrowSystemDefaultPreview() {
    AppTheme(phase = TimerPhase.BREAK, themeMode = ThemeMode.LIGHT) {
        TimerScreen(
            state = TimerState(
                phase = TimerPhase.BREAK,
                remainingMillis = 3 * 60 * 1000L,
                cyclePosition = 27 * 60 * 1000L,
            ),
            isTracking = true,
            onStart = {},
            onStop = {},
            currentLanguageTag = null,
            supportedLanguages = listOf(
                LocaleManager.AppLanguage(tag = null, displayName = null),
                LocaleManager.AppLanguage(tag = "es", displayName = "Español"),
            ),
            onLanguageRowClick = {},
            themeMode = ThemeMode.LIGHT,
            onThemeToggle = {},
            soundSettings = SoundSettingsState(),
            onSoundSelected = { _, _ -> },
        )
    }
}

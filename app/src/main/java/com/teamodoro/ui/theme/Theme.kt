package com.teamodoro.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.ThemeMode

private val WorkLightColorScheme = lightColorScheme(
    primary = WorkPrimary,
    onPrimary = WorkOnPrimary,
    primaryContainer = WorkPrimaryContainer,
    onPrimaryContainer = WorkOnPrimaryContainer,
    secondary = WorkSecondary,
    background = WorkBackground,
    surface = WorkSurface,
    onBackground = WorkOnBackground,
    onSurface = WorkOnSurface,
)

private val BreakLightColorScheme = lightColorScheme(
    primary = BreakPrimary,
    onPrimary = BreakOnPrimary,
    primaryContainer = BreakPrimaryContainer,
    onPrimaryContainer = BreakOnPrimaryContainer,
    secondary = BreakSecondary,
    background = BreakBackground,
    surface = BreakSurface,
    onBackground = BreakOnBackground,
    onSurface = BreakOnSurface,
)

private val WorkDarkColorScheme = darkColorScheme(
    primary = WorkDarkPrimary,
    onPrimary = WorkDarkOnPrimary,
    primaryContainer = WorkDarkPrimaryContainer,
    onPrimaryContainer = WorkDarkOnPrimaryContainer,
    secondary = WorkDarkSecondary,
    background = WorkDarkBackground,
    surface = WorkDarkSurface,
    onBackground = WorkDarkOnBackground,
    onSurface = WorkDarkOnSurface,
)

private val BreakDarkColorScheme = darkColorScheme(
    primary = BreakDarkPrimary,
    onPrimary = BreakDarkOnPrimary,
    primaryContainer = BreakDarkPrimaryContainer,
    onPrimaryContainer = BreakDarkOnPrimaryContainer,
    secondary = BreakDarkSecondary,
    background = BreakDarkBackground,
    surface = BreakDarkSurface,
    onBackground = BreakDarkOnBackground,
    onSurface = BreakDarkOnSurface,
)

private const val COLOR_ANIM_DURATION_MS = 600

@Composable
fun AppTheme(
    phase: TimerPhase,
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val targetScheme = when {
        phase == TimerPhase.WORK && darkTheme -> WorkDarkColorScheme
        phase == TimerPhase.WORK -> WorkLightColorScheme
        darkTheme -> BreakDarkColorScheme
        else -> BreakLightColorScheme
    }

    val primary by animateColorAsState(targetScheme.primary, tween(COLOR_ANIM_DURATION_MS), label = "primary")
    val onPrimary by animateColorAsState(targetScheme.onPrimary, tween(COLOR_ANIM_DURATION_MS), label = "onPrimary")
    val primaryContainer by animateColorAsState(targetScheme.primaryContainer, tween(COLOR_ANIM_DURATION_MS), label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetScheme.onPrimaryContainer, tween(COLOR_ANIM_DURATION_MS), label = "onPrimaryContainer")
    val secondary by animateColorAsState(targetScheme.secondary, tween(COLOR_ANIM_DURATION_MS), label = "secondary")
    val background by animateColorAsState(targetScheme.background, tween(COLOR_ANIM_DURATION_MS), label = "background")
    val surface by animateColorAsState(targetScheme.surface, tween(COLOR_ANIM_DURATION_MS), label = "surface")
    val onBackground by animateColorAsState(targetScheme.onBackground, tween(COLOR_ANIM_DURATION_MS), label = "onBackground")
    val onSurface by animateColorAsState(targetScheme.onSurface, tween(COLOR_ANIM_DURATION_MS), label = "onSurface")

    val animatedScheme = targetScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        background = background,
        surface = surface,
        onBackground = onBackground,
        onSurface = onSurface,
    )

    MaterialTheme(
        colorScheme = animatedScheme,
        content = content,
    )
}

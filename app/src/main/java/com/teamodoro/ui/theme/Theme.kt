package com.teamodoro.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.teamodoro.domain.TimerPhase

private val WorkColorScheme = lightColorScheme(
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

private val BreakColorScheme = lightColorScheme(
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

private const val COLOR_ANIM_DURATION_MS = 600

@Composable
fun TeamodoroTheme(
    phase: TimerPhase,
    content: @Composable () -> Unit,
) {
    val targetScheme = if (phase == TimerPhase.WORK) WorkColorScheme else BreakColorScheme

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

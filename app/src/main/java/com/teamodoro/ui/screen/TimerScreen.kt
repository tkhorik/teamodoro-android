package com.teamodoro.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
import com.teamodoro.domain.WORK_DURATION_MILLIS
import com.teamodoro.ui.theme.TeamodoroTheme

@Composable
fun TimerScreen(
    state: TimerState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PhaseLabel(phase = state.phase)

            Spacer(modifier = Modifier.height(40.dp))

            TimerRing(state = state)

            Spacer(modifier = Modifier.height(48.dp))

            ControlButtons(
                isRunning = state.isRunning,
                onStart = onStart,
                onStop = onStop,
            )
        }
    }
}

@Composable
private fun PhaseLabel(phase: TimerPhase) {
    val label = if (phase == TimerPhase.WORK) "Focus" else "Break"
    Text(
        text = label,
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
                text = "remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun ControlButtons(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    if (isRunning) {
        OutlinedButton(onClick = onStop) {
            Text("Stop Timer")
        }
    } else {
        Button(onClick = onStart) {
            Text("Start Timer")
        }
    }
}

private fun Long.toMinutesSeconds(): String {
    val totalSeconds = this / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenWorkPreview() {
    TeamodoroTheme(phase = TimerPhase.WORK) {
        TimerScreen(
            state = TimerState(
                phase = TimerPhase.WORK,
                remainingMillis = 72 * 60 * 1000L,
                cyclePosition = 28 * 60 * 1000L,
                isRunning = true,
            ),
            onStart = {},
            onStop = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerScreenBreakPreview() {
    TeamodoroTheme(phase = TimerPhase.BREAK) {
        TimerScreen(
            state = TimerState(
                phase = TimerPhase.BREAK,
                remainingMillis = 20 * 60 * 1000L,
                cyclePosition = 110 * 60 * 1000L,
                isRunning = true,
            ),
            onStart = {},
            onStop = {},
        )
    }
}

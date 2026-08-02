package com.teamodoro.service

import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState

/** Display text separated from Android notification construction for JVM tests. */
internal object NotificationContent {

    fun timer(state: TimerState): Content {
        val phaseLabel = if (state.phase == TimerPhase.WORK) "Focus" else "Break"
        return Content(
            title = "$phaseLabel — ${state.remainingMillis.toMinutesSeconds()} remaining",
            text = "Teamodoro is running",
        )
    }

    fun transition(newPhase: TimerPhase): Content = when (newPhase) {
        TimerPhase.WORK -> Content(
            title = "Focus time!",
            text = "25-minute focus session started",
        )
        TimerPhase.BREAK -> Content(
            title = "Take a break!",
            text = "5-minute break started",
        )
    }

    private fun Long.toMinutesSeconds(): String {
        val totalSeconds = this / 1_000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}

internal data class Content(
    val title: String,
    val text: String,
)

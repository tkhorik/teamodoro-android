package com.teamodoro.service

import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
import java.util.Locale

/** Display text separated from Android notification construction for JVM tests. */
internal object NotificationContent {

    fun timer(state: TimerState, copy: NotificationCopy = NotificationCopy.ENGLISH): Content {
        val phaseLabel = if (state.phase == TimerPhase.WORK) copy.focusLabel else copy.breakLabel
        return Content(
            title = String.format(Locale.ROOT, copy.timerTitleFormat, phaseLabel, state.remainingMillis.toMinutesSeconds()),
            text = copy.timerText,
        )
    }

    fun transition(
        newPhase: TimerPhase,
        copy: NotificationCopy = NotificationCopy.ENGLISH,
    ): Content = when (newPhase) {
        TimerPhase.WORK -> Content(
            title = copy.focusTransitionTitle,
            text = copy.focusTransitionText,
        )
        TimerPhase.BREAK -> Content(
            title = copy.breakTransitionTitle,
            text = copy.breakTransitionText,
        )
    }

    private fun Long.toMinutesSeconds(): String {
        val totalSeconds = this / 1_000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}

/** Localized wording supplied by the Android-facing notification helper. */
internal data class NotificationCopy(
    val focusLabel: String,
    val breakLabel: String,
    val timerTitleFormat: String,
    val timerText: String,
    val focusTransitionTitle: String,
    val focusTransitionText: String,
    val breakTransitionTitle: String,
    val breakTransitionText: String,
) {
    companion object {
        val ENGLISH = NotificationCopy(
            focusLabel = "Focus",
            breakLabel = "Break",
            timerTitleFormat = "%1\$s — %2\$s remaining",
            timerText = "Teamodoro is running",
            focusTransitionTitle = "Focus time!",
            focusTransitionText = "25-minute focus session started",
            breakTransitionTitle = "Take a break!",
            breakTransitionText = "5-minute break started",
        )
    }
}

internal data class Content(
    val title: String,
    val text: String,
)

package com.teamodoro.ui.screen

import com.teamodoro.domain.BREAK_DURATION_MILLIS
import com.teamodoro.domain.CYCLE_MILLIS
import com.teamodoro.domain.WORK_DURATION_MILLIS
import java.util.concurrent.TimeUnit

/**
 * Minute values shown in [AboutDialog], derived from the cycle constants so the
 * guide can never disagree with the timer.
 */
internal object AboutGuide {
    val focusMinutes: Int = TimeUnit.MILLISECONDS.toMinutes(WORK_DURATION_MILLIS).toInt()
    val breakMinutes: Int = TimeUnit.MILLISECONDS.toMinutes(BREAK_DURATION_MILLIS).toInt()
    val cycleMinutes: Int = TimeUnit.MILLISECONDS.toMinutes(CYCLE_MILLIS).toInt()
}

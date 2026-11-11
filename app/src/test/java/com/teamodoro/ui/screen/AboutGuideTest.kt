package com.teamodoro.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class AboutGuideTest {

    @Test
    fun `guide minutes match the cycle constants`() {
        assertEquals(25, AboutGuide.focusMinutes)
        assertEquals(5, AboutGuide.breakMinutes)
        assertEquals(30, AboutGuide.cycleMinutes)
    }

    @Test
    fun `focus plus break equals one cycle`() {
        assertEquals(AboutGuide.cycleMinutes, AboutGuide.focusMinutes + AboutGuide.breakMinutes)
    }
}

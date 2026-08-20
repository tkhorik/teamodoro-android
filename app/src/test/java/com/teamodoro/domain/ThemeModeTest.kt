package com.teamodoro.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `toggle switches explicit light and dark modes`() {
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.toggled(systemIsDark = false))
        assertEquals(ThemeMode.LIGHT, ThemeMode.DARK.toggled(systemIsDark = true))
    }

    @Test
    fun `toggle converts system mode to the opposite explicit system appearance`() {
        assertEquals(ThemeMode.DARK, ThemeMode.SYSTEM.toggled(systemIsDark = false))
        assertEquals(ThemeMode.LIGHT, ThemeMode.SYSTEM.toggled(systemIsDark = true))
    }
}

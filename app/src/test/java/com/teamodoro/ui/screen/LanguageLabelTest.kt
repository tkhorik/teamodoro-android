package com.teamodoro.ui.screen

import com.teamodoro.locale.LocaleManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageLabelTest {

    private val supportedLanguages = listOf(
        LocaleManager.AppLanguage(tag = null, displayName = null),
        LocaleManager.AppLanguage(tag = "en", displayName = "English"),
        LocaleManager.AppLanguage(tag = "uk", displayName = "Українська"),
    )

    @Test
    fun `supported tag returns its display name`() {
        assertEquals("Українська", resolveLanguageLabel("uk", supportedLanguages))
    }

    @Test
    fun `null tag returns null for system default`() {
        assertNull(resolveLanguageLabel(null, supportedLanguages))
    }

    @Test
    fun `unsupported tag falls back to its own-locale display name`() {
        assertEquals("italiano", resolveLanguageLabel("it", supportedLanguages))
    }
}

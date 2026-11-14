package com.teamodoro.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.teamodoro.data.SoundEvent
import com.teamodoro.data.TransitionSound
import com.teamodoro.domain.ThemeMode
import com.teamodoro.locale.LocaleManager
import com.teamodoro.ui.SoundSettingsState
import com.teamodoro.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val languages = listOf(
        LocaleManager.AppLanguage(tag = null, displayName = null),
        LocaleManager.AppLanguage(tag = "uk", displayName = "Українська"),
    )

    @Test
    fun timerScreenOpensSettingsWithoutMainScreenLanguageControl() {
        composeRule.setContent {
            AppTheme(phase = com.teamodoro.domain.TimerPhase.WORK, themeMode = ThemeMode.LIGHT) {
                TimerScreen(
                    state = com.teamodoro.domain.TimerState(
                        phase = com.teamodoro.domain.TimerPhase.WORK,
                        remainingMillis = 17 * 60 * 1000L,
                        cyclePosition = 8 * 60 * 1000L,
                    ),
                    isTracking = false,
                    onStart = {},
                    onStop = {},
                    currentLanguageTag = "uk",
                    supportedLanguages = languages,
                    onLanguageRowClick = {},
                    themeMode = ThemeMode.LIGHT,
                    onThemeToggle = {},
                    soundSettings = SoundSettingsState(),
                    onSoundSelected = { _, _ -> },
                    focusTickEnabled = false,
                    onFocusTickToggle = {},
                )
            }
        }

        assertEquals(0, composeRule.onAllNodesWithText("Українська").fetchSemanticsNodes().size)
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Settings").fetchSemanticsNode()
        composeRule.onNodeWithText("Notification sounds").fetchSemanticsNode()
    }

    @Test
    fun selectedLanguageRowIsOneAccessibleTargetAndDismissesBeforeCallback() {
        verifyLanguageRow(currentTag = "uk", expectedValue = "Українська")
    }

    @Test
    fun systemDefaultLanguageIsShownAndRowDismissesBeforeCallback() {
        verifyLanguageRow(currentTag = null, expectedValue = "System default")
    }

    private fun verifyLanguageRow(currentTag: String?, expectedValue: String) {
        var clicks = 0
        lateinit var isVisible: androidx.compose.runtime.MutableState<Boolean>
        composeRule.setContent {
            isVisible = remember { mutableStateOf(true) }
            AppTheme(phase = com.teamodoro.domain.TimerPhase.WORK, themeMode = ThemeMode.LIGHT) {
                if (isVisible.value) {
                    SettingsDialog(
                        state = SoundSettingsState(),
                        currentLanguageTag = currentTag,
                        supportedLanguages = languages,
                        onSoundSelected = { _: SoundEvent, _: TransitionSound -> },
                        onLanguageClick = {
                            isVisible.value = false
                            clicks++
                        },
                        onDismiss = { isVisible.value = false },
                    )
                } else {
                    Text("Dialog dismissed")
                }
            }
        }

        val languageTarget = composeRule.onNodeWithContentDescription("App language: $expectedValue")
        languageTarget.fetchSemanticsNode()
        composeRule.onNodeWithText(expectedValue).fetchSemanticsNode()
        composeRule.runOnIdle {
            val minimumHeightPx = with(composeRule.density) { 48.dp.toPx() }
            assert(languageTarget.fetchSemanticsNode().boundsInRoot.height >= minimumHeightPx)
        }
        languageTarget.performClick()
        composeRule.onNodeWithText("Dialog dismissed").fetchSemanticsNode()
        composeRule.runOnIdle { assertEquals(1, clicks) }
    }
}

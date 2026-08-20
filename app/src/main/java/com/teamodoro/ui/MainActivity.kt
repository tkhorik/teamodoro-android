package com.teamodoro.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teamodoro.ui.screen.LanguagePickerDialog
import com.teamodoro.ui.screen.TimerScreen
import com.teamodoro.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

// AppCompatActivity, not ComponentActivity: AppCompatDelegate.setApplicationLocales()
// (see LocaleManager) only takes effect on API 26-32 when the activity extends
// AppCompatActivity — a plain ComponentActivity silently no-ops there. Compose
// works the same either way, so this costs nothing on API 33+.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: TimerViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Proceed regardless — notifications are non-critical
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            val state by viewModel.timerState.collectAsStateWithLifecycle()
            val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
            val currentLanguageTag by viewModel.currentLanguageTag.collectAsStateWithLifecycle()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            var showLanguageDialog by remember { mutableStateOf(false) }

            AppTheme(phase = state.phase, themeMode = themeMode) {
                TimerScreen(
                    state = state,
                    isTracking = isTracking,
                    onStart = viewModel::startTimer,
                    onStop = viewModel::stopTimer,
                    currentLanguageTag = currentLanguageTag,
                    supportedLanguages = viewModel.supportedLanguages,
                    onLanguageRowClick = {
                        // API 33+: the OS has its own per-app language screen — prefer
                        // it, since it also covers other locale-aware apps at once.
                        // Below that there is no such screen, so fall back to an
                        // in-app picker backed by AppCompatDelegate.
                        if (viewModel.systemLanguagePickerAvailable) {
                            startActivity(viewModel.systemLanguageSettingsIntent())
                        } else {
                            showLanguageDialog = true
                        }
                    },
                    themeMode = themeMode,
                    onThemeToggle = themeViewModel::toggleTheme,
                )

                if (showLanguageDialog) {
                    LanguagePickerDialog(
                        languages = viewModel.supportedLanguages,
                        currentTag = currentLanguageTag,
                        onLanguageSelected = { tag ->
                            viewModel.setLanguage(tag)
                            showLanguageDialog = false
                        },
                        onDismiss = { showLanguageDialog = false },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // On Android 13+, the language may have been changed in the system
        // Settings screen opened from this Activity. The ViewModel survives a
        // configuration change, so refresh its selection label on return.
        viewModel.refreshCurrentLanguage()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

package com.teamodoro.ui

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamodoro.data.ThemePreferencesRepository
import com.teamodoro.domain.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferencesRepository: ThemePreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    fun toggleTheme() {
        viewModelScope.launch {
            themePreferencesRepository.setThemeMode(
                themeMode.value.toggled(systemIsDark = isSystemInDarkTheme()),
            )
        }
    }

    private fun isSystemInDarkTheme(): Boolean =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
}

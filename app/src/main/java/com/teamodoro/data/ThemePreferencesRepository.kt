package com.teamodoro.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teamodoro.domain.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

/** Persists the user's appearance override independently from timer preferences. */
@Singleton
class ThemePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        preferences[THEME_MODE]
            ?.let { storedValue -> ThemeMode.entries.firstOrNull { it.name == storedValue } }
            ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    private companion object {
        val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
    }
}

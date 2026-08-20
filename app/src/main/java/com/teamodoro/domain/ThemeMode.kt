package com.teamodoro.domain

/** The user's persisted appearance preference. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    /** Returns the explicit mode selected by the app-bar toggle. */
    fun toggled(systemIsDark: Boolean): ThemeMode = when (this) {
        LIGHT -> DARK
        DARK -> LIGHT
        SYSTEM -> if (systemIsDark) LIGHT else DARK
    }
}

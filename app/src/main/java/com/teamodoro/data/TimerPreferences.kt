package com.teamodoro.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only durable state this app has: whether the user has switched tracking on.
 *
 * The cycle itself needs no persistence — it is derived from the clock. This
 * flag only records whether the foreground service and its ongoing notification
 * should be running, so the UI, the QS tile and [com.teamodoro.service.BootReceiver]
 * agree after a process death or a reboot.
 *
 * SharedPreferences rather than Room deliberately: one boolean does not justify
 * a database, a DAO, a schema version or a migration path.
 */
@Singleton
class TimerPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isTracking: Boolean
        get() = prefs.getBoolean(KEY_TRACKING, false)
        set(value) = prefs.edit().putBoolean(KEY_TRACKING, value).apply()

    /** Emits the current value immediately, then on every change. */
    val isTrackingFlow: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_TRACKING) trySend(isTracking)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(isTracking)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val PREFS_NAME = "teamodoro_prefs"
        const val KEY_TRACKING = "is_tracking"
    }
}

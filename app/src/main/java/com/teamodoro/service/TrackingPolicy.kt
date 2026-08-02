package com.teamodoro.service

/**
 * Pure rules for deciding whether a background event may resume tracking.
 *
 * AlarmManager broadcasts can already be in flight when the user turns
 * notifications off, so their action alone is never enough to restart the
 * foreground service. The persisted tracking preference is authoritative.
 */
internal object TrackingPolicy {

    fun shouldHandlePhaseTransition(action: String?, isTracking: Boolean): Boolean =
        action == AlarmScheduler.ACTION_PHASE_TRANSITION && isTracking

    fun shouldRestoreAfterBoot(action: String?, isTracking: Boolean): Boolean =
        action == ACTION_BOOT_COMPLETED && isTracking

    const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
}

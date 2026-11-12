package com.teamodoro.data

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Asks the system about a stored custom sound: can it still be opened, and what is it called. */
@Singleton
class CustomSoundResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** A sound counts as available when the system lets us open it; any failure means it is gone. */
    fun isAvailable(uri: String): Boolean = try {
        context.contentResolver.openAssetFileDescriptor(Uri.parse(uri), "r")?.use { true } ?: false
    } catch (e: Exception) {
        false
    }

    fun title(uri: String): String? = try {
        RingtoneManager.getRingtone(context, Uri.parse(uri))?.getTitle(context)
    } catch (e: Exception) {
        Log.w(TAG, "Could not read the title of a custom sound", e)
        null
    }

    private companion object {
        const val TAG = "CustomSoundResolver"
    }
}

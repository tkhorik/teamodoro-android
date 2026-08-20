package com.teamodoro.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.teamodoro.R
import com.teamodoro.data.TimerPreferences
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
import com.teamodoro.locale.LocaleManager
import com.teamodoro.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class TeamodoroTileService : TileService() {

    @Inject
    lateinit var calculateTimerUseCase: CalculateTimerUseCase

    @Inject
    lateinit var preferences: TimerPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    private var tileScope: CoroutineScope? = null
    private var timerJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        tileScope = CoroutineScope(Dispatchers.Main + Job())
        timerJob = calculateTimerUseCase.timerStateFlow()
            .onEach { state -> updateTile(state) }
            .launchIn(tileScope!!)
    }

    override fun onStopListening() {
        super.onStopListening()
        timerJob?.cancel()
        tileScope?.cancel()
        tileScope = null
    }

    // The PendingIntent overload of startActivityAndCollapse only exists on API 34+,
    // so the deprecated Intent overload is the only option below that. The version
    // check below already routes around it; lint flags the call regardless.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTile(state: TimerState) {
        val tile = qsTile ?: return

        val localizedContext = localeManager.localizedContext()
        val phaseLabel = localizedContext.getString(
            if (state.phase == TimerPhase.WORK) R.string.phase_focus else R.string.phase_break,
        )
        val timeText = state.remainingMillis.toMinutesSeconds()

        // Tile.setSubtitle is API 29+; on 26-28 it throws NoSuchMethodError.
        // Fold the phase and time into the label there instead.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.label = localizedContext.getString(R.string.tile_label)
            tile.subtitle = "$phaseLabel · $timeText"
        } else {
            tile.label = "$phaseLabel · $timeText"
        }
        // The cycle always runs, so ACTIVE reflects whether this device is
        // tracking it — not whether a timer is going.
        tile.state = if (preferences.isTracking) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    private fun Long.toMinutesSeconds(): String {
        val totalSeconds = this / 1_000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}

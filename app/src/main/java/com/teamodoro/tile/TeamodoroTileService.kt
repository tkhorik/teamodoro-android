package com.teamodoro.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.teamodoro.data.RoomRepository
import com.teamodoro.domain.CalculateTimerUseCase
import com.teamodoro.domain.TimerPhase
import com.teamodoro.domain.TimerState
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

        val phaseLabel = if (state.phase == TimerPhase.WORK) "Focus" else "Break"
        val timeText = state.remainingMillis.toMinutesSeconds()

        tile.label = "Teamodoro"
        tile.subtitle = "$phaseLabel · $timeText"
        tile.state = if (state.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    private fun Long.toMinutesSeconds(): String {
        val totalSeconds = this / 1_000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}

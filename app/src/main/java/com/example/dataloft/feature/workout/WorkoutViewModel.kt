package com.example.dataloft.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dataloft.workout.WorkoutSessionManager
import com.example.dataloft.workout.WorkoutSessionState
import com.example.dataloft.workout.WorkoutStatus
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit

class WorkoutViewModel(
    private val sessionManager: WorkoutSessionManager
) : ViewModel() {

    val uiState: StateFlow<WorkoutUiState> = sessionManager.sessionState
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutUiState()
        )

    fun onStartStopRequested() {
        if (sessionManager.sessionStateValue().status == WorkoutStatus.Running) {
            sessionManager.stop(manual = true)
        } else {
            sessionManager.start()
        }
    }

    private fun WorkoutSessionState.toUiState(): WorkoutUiState {
        val timer = timer
        val formattedTotal = formatDuration(timer?.totalTime ?: 0)
        val formattedElapsed = formatDurationMillis(totalElapsedMs)
        val intervalsUi = timer?.intervals?.mapIndexed { index, interval ->
            WorkoutIntervalUi(
                title = interval.title,
                durationLabel = formatDuration(interval.time),
                durationSeconds = interval.time,
                progress = if (index < currentIntervalIndex) 1f
                else if (index == currentIntervalIndex && interval.time > 0)
                    (elapsedInIntervalMs / (interval.time * 1000f)).coerceIn(0f, 1f)
                else 0f,
                isActive = index == currentIntervalIndex
            )
        } ?: emptyList()

        val buttonLabel = if (status == WorkoutStatus.Running) "Стоп" else "Старт"
        val isStartEnabled = timer != null
        return WorkoutUiState(
            timerTitle = timer?.title.orEmpty(),
            totalDurationLabel = formattedTotal,
            totalElapsedLabel = formattedElapsed,
            intervals = intervalsUi,
            currentIntervalIndex = currentIntervalIndex,
            currentIntervalRemainingLabel = if (timer != null && timer.intervals.isNotEmpty()) {
                val interval = timer.intervals[currentIntervalIndex.coerceAtMost(timer.intervals.lastIndex)]
                formatDurationMillis(interval.time * 1000L - elapsedInIntervalMs)
            } else {
                "00:00"
            },
            status = status,
            track = track,
            buttonLabel = buttonLabel,
            isStartEnabled = isStartEnabled
        )
    }

    data class WorkoutUiState(
        val timerTitle: String = "",
        val totalDurationLabel: String = "00:00",
        val totalElapsedLabel: String = "00:00",
        val currentIntervalRemainingLabel: String = "00:00",
        val intervals: List<WorkoutIntervalUi> = emptyList(),
        val currentIntervalIndex: Int = 0,
        val status: WorkoutStatus = WorkoutStatus.Idle,
        val track: List<LatLng> = emptyList(),
        val buttonLabel: String = "Старт",
        val isStartEnabled: Boolean = false
    )

    data class WorkoutIntervalUi(
        val title: String,
        val durationLabel: String,
        val durationSeconds: Int,
        val progress: Float,
        val isActive: Boolean
    )

    companion object {
        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val sec = seconds % 60
            return String.format("%02d:%02d", minutes, sec)
        }

        private fun formatDurationMillis(millis: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}

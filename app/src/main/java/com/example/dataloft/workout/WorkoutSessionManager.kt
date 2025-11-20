package com.example.dataloft.workout

import android.os.SystemClock
import com.example.dataloft.domain.model.IntervalTimer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

class WorkoutSessionManager(
    private val trackGenerator: FakeTrackGenerator,
    private val audioCuePlayer: WorkoutAudioCuePlayer,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _sessionState = MutableStateFlow(WorkoutSessionState())
    val sessionState: StateFlow<WorkoutSessionState> = _sessionState.asStateFlow()

    private var tickerJob: Job? = null
    private var workoutStartRealtime: Long = 0L
    private var lastAnnouncedInterval = -1

    fun loadTimer(timer: IntervalTimer) {
        stopTicker()
        trackGenerator.reset()
        lastAnnouncedInterval = -1
        _sessionState.value = WorkoutSessionState(timer = timer)
    }

    fun start() {
        val timer = _sessionState.value.timer ?: return
        val state = _sessionState.value
        if (state.status == WorkoutStatus.Running) return

        val shouldReset = state.status == WorkoutStatus.Completed ||
            state.status == WorkoutStatus.Stopped ||
            state.totalElapsedMs >= timer.totalTime * 1000L

        if (shouldReset) {
            trackGenerator.reset()
            _sessionState.update {
                it.copy(
                    totalElapsedMs = 0L,
                    elapsedInIntervalMs = 0L,
                    currentIntervalIndex = 0,
                    track = emptyList(),
                    status = WorkoutStatus.Idle
                )
            }
        }

        workoutStartRealtime = SystemClock.elapsedRealtime() - _sessionState.value.totalElapsedMs
        if (_sessionState.value.totalElapsedMs == 0L || shouldReset) {
            audioCuePlayer.pulse()
            lastAnnouncedInterval = -1
        } else {
            lastAnnouncedInterval = _sessionState.value.currentIntervalIndex
        }
        launchTicker()
    }

    fun stop(manual: Boolean = true) {
        stopTicker()
        _sessionState.update {
            it.copy(status = if (manual) WorkoutStatus.Stopped else WorkoutStatus.Completed)
        }
    }

    private fun launchTicker() {
        stopTicker()
        tickerJob = scope.launch {
            while (isActive) {
                val timer = _sessionState.value.timer ?: break
                val durationMs = timer.totalTime * 1000L
                if (durationMs <= 0L) break
                val elapsedMs = min(SystemClock.elapsedRealtime() - workoutStartRealtime, durationMs)
                val progress = resolveProgress(timer, elapsedMs)
                val newTrack = trackGenerator.trackForProgress(elapsedMs, durationMs)
                _sessionState.update {
                    it.copy(
                        status = if (progress.finished && elapsedMs >= durationMs) WorkoutStatus.Completed else WorkoutStatus.Running,
                        totalElapsedMs = elapsedMs,
                        currentIntervalIndex = progress.intervalIndex,
                        elapsedInIntervalMs = progress.elapsedInIntervalMs,
                        track = newTrack
                    )
                }

                if (progress.intervalIndex != lastAnnouncedInterval && lastAnnouncedInterval != -2) {
                    if (lastAnnouncedInterval >= 0 || (lastAnnouncedInterval == -1 && progress.intervalIndex > 0)) {
                        audioCuePlayer.pulse()
                    }
                    lastAnnouncedInterval = progress.intervalIndex
                }

                if (progress.finished && elapsedMs >= durationMs) {
                    audioCuePlayer.pulse(times = 2)
                    stop(manual = false)
                    break
                }
                delay(100L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun resolveProgress(timer: IntervalTimer, elapsedMs: Long): IntervalProgress {
        if (timer.intervals.isEmpty()) {
            return IntervalProgress(
                intervalIndex = 0,
                elapsedInIntervalMs = 0L,
                finished = true
            )
        }
        var remaining = elapsedMs
        timer.intervals.forEachIndexed { index, interval ->
            val intervalDurationMs = interval.time * 1000L
            if (remaining < intervalDurationMs) {
                return IntervalProgress(
                    intervalIndex = index,
                    elapsedInIntervalMs = remaining,
                    finished = false
                )
            }
            remaining -= intervalDurationMs
        }
        return IntervalProgress(
            intervalIndex = timer.intervals.lastIndex,
            elapsedInIntervalMs = timer.intervals.last().time * 1000L,
            finished = true
        )
    }

    fun sessionStateValue(): WorkoutSessionState = _sessionState.value

    data class IntervalProgress(
        val intervalIndex: Int,
        val elapsedInIntervalMs: Long,
        val finished: Boolean
    )
}

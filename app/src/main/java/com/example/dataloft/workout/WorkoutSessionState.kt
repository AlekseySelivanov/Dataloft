package com.example.dataloft.workout

import com.example.dataloft.domain.model.IntervalTimer
import com.google.android.gms.maps.model.LatLng

data class WorkoutSessionState(
    val timer: IntervalTimer? = null,
    val status: WorkoutStatus = WorkoutStatus.Idle,
    val totalElapsedMs: Long = 0L,
    val currentIntervalIndex: Int = 0,
    val elapsedInIntervalMs: Long = 0L,
    val track: List<LatLng> = emptyList()
) {
    val isActive: Boolean = status == WorkoutStatus.Running
    val totalDurationMs: Long = (timer?.totalTime ?: 0) * 1000L
}

enum class WorkoutStatus {
    Idle,
    Running,
    Completed,
    Stopped
}

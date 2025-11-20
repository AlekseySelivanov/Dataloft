package com.example.dataloft.domain.model

data class IntervalTimer(
    val id: Int,
    val title: String,
    val totalTime: Int,
    val intervals: List<WorkoutInterval>
) {
    val totalIntervals: Int = intervals.size
}

data class WorkoutInterval(
    val title: String,
    val time: Int
)

package com.example.dataloft.data.remote

import com.example.dataloft.data.remote.dto.TimerDto
import com.example.dataloft.domain.model.IntervalTimer
import com.example.dataloft.domain.model.WorkoutInterval

fun TimerDto.toDomain(): IntervalTimer = IntervalTimer(
    id = id,
    title = title,
    totalTime = totalTime,
    intervals = intervals.map { WorkoutInterval(title = it.title, time = it.time) }
)

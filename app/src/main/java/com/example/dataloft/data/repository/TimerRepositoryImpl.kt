package com.example.dataloft.data.repository

import com.example.dataloft.data.remote.TimerApi
import com.example.dataloft.data.remote.toDomain
import com.example.dataloft.domain.model.IntervalTimer
import com.example.dataloft.domain.repository.TimerRepository

class TimerRepositoryImpl(
    private val timerApi: TimerApi
) : TimerRepository {
    override suspend fun loadTimer(timerId: Int): IntervalTimer {
        val response = timerApi.getTimer(timerId)
        return response.timer.toDomain()
    }
}

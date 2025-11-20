package com.example.dataloft.domain.repository

import com.example.dataloft.domain.model.IntervalTimer

interface TimerRepository {
    suspend fun loadTimer(timerId: Int): IntervalTimer
}

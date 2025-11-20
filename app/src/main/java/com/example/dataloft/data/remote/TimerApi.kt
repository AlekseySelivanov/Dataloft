package com.example.dataloft.data.remote

import com.example.dataloft.data.remote.dto.TimerResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface TimerApi {
    @GET("interval-timers/{id}")
    suspend fun getTimer(@Path("id") id: Int): TimerResponseDto
}

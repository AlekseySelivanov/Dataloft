package com.example.dataloft.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimerDto(
    @SerialName("timer_id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("total_time") val totalTime: Int,
    @SerialName("intervals") val intervals: List<IntervalDto>
)

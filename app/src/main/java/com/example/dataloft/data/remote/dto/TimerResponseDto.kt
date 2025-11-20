package com.example.dataloft.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimerResponseDto(
    @SerialName("timer") val timer: TimerDto
)

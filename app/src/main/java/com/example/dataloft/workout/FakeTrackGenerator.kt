package com.example.dataloft.workout

import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

class FakeTrackGenerator {
    private val basePath = listOf(
        LatLng(55.752220, 37.615560),
        LatLng(55.752450, 37.616980),
        LatLng(55.753120, 37.618400),
        LatLng(55.754100, 37.619100),
        LatLng(55.755100, 37.618300),
        LatLng(55.756200, 37.617100),
        LatLng(55.757100, 37.616700),
        LatLng(55.758000, 37.617400),
        LatLng(55.758500, 37.619300),
        LatLng(55.758300, 37.621100),
        LatLng(55.757400, 37.622500),
        LatLng(55.756200, 37.623200),
        LatLng(55.755100, 37.622700),
        LatLng(55.754200, 37.621500),
        LatLng(55.753300, 37.620000),
        LatLng(55.752600, 37.618500),
        LatLng(55.752200, 37.616900)
    )

    private var latestPoints: Int = 1

    fun reset() {
        latestPoints = 1
    }

    fun trackForProgress(elapsedMs: Long, totalMs: Long): List<LatLng> {
        if (totalMs <= 0L) return basePath.take(latestPoints)
        val ratio = (elapsedMs.toDouble() / totalMs).coerceIn(0.0, 1.0)
        val targetPoints = (ratio * basePath.size).roundToInt().coerceAtLeast(1).coerceAtMost(basePath.size)
        if (targetPoints > latestPoints) {
            latestPoints = targetPoints
        }
        return basePath.take(latestPoints)
    }
}

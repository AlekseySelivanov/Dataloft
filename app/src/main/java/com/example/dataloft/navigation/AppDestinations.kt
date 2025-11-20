package com.example.dataloft.navigation

object AppDestinations {
    const val LOAD = "load"
    const val WORKOUT = "workout/{timerId}"

    fun workout(timerId: Int) = "workout/$timerId"
}

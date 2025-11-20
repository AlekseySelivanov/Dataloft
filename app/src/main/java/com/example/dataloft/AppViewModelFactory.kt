package com.example.dataloft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dataloft.di.ServiceLocator
import com.example.dataloft.feature.load.LoadTimerViewModel
import com.example.dataloft.feature.workout.WorkoutViewModel

class AppViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val sessionManager = ServiceLocator.workoutSessionManager
        return when {
            modelClass.isAssignableFrom(LoadTimerViewModel::class.java) -> {
                LoadTimerViewModel(
                    repository = ServiceLocator.timerRepository,
                    sessionManager = sessionManager
                ) as T
            }

            modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> {
                WorkoutViewModel(sessionManager = sessionManager) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}

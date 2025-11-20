package com.example.dataloft.feature.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dataloft.domain.repository.TimerRepository
import com.example.dataloft.workout.WorkoutSessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_TIMER_ID = "68"

class LoadTimerViewModel(
    private val repository: TimerRepository,
    private val sessionManager: WorkoutSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoadUiState())
    val uiState: StateFlow<LoadUiState> = _uiState.asStateFlow()

    private val eventsChannel = Channel<LoadEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    fun onTimerIdChanged(input: String) {
        val filtered = input.filter { it.isDigit() }.take(5)
        _uiState.update { it.copy(timerIdInput = filtered, errorMessage = null) }
    }

    fun loadTimer() {
        val timerId = _uiState.value.timerIdInput.toIntOrNull()
        if (timerId == null) {
            _uiState.update { it.copy(errorMessage = "Введите ID тренировки") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loadTimer(timerId) }
                .onSuccess { timer ->
                    sessionManager.loadTimer(timer)
                    eventsChannel.send(LoadEvent.NavigateToWorkout(timer.id))
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Не удалось загрузить тренировку"
                        )
                    }
                }
        }
    }

}

data class LoadUiState(
    val timerIdInput: String = DEFAULT_TIMER_ID,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoadEvent {
    data class NavigateToWorkout(val timerId: Int) : LoadEvent
}

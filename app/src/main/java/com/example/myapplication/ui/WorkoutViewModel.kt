package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ExerciseDbModel
import com.example.myapplication.data.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _uiState.value = WorkoutUiState.Loading
            workoutRepository.getWorkoutSuggestions()
                .catch { e ->
                    _uiState.value = WorkoutUiState.Error("Failed to load suggestions: ${e.message}")
                }
                .collect { suggestions ->
                    if (suggestions.isEmpty()) {
                        _uiState.value = WorkoutUiState.Empty
                    } else {
                        _uiState.value = WorkoutUiState.Success(suggestions)
                    }
                }
        }
    }
}

sealed class WorkoutUiState {
    object Loading : WorkoutUiState()
    data class Success(val suggestions: List<ExerciseDbModel>) : WorkoutUiState()
    data class Error(val message: String) : WorkoutUiState()
    object Empty : WorkoutUiState()
}

class WorkoutViewModelFactory(
    private val workoutRepository: WorkoutRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(workoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

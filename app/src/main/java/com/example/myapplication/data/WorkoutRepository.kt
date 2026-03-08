package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WorkoutRepository {
    private val apiService = RetrofitInstance.api

    fun getWorkoutSuggestions(): Flow<List<WorkoutSuggestion>> = flow {
        val response = apiService.getWorkouts()
        // Filter out items without names or descriptions to ensure quality tips
        val filteredResults = response.results.filter { 
            it.name.isNotBlank() && it.description.isNotBlank() 
        }
        emit(filteredResults)
    }
}

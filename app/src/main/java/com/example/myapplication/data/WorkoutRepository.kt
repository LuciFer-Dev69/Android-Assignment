package com.example.myapplication.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WorkoutRepository {
    private val TAG = "WorkoutRepository"
    private val exerciseDbApi = RetrofitInstance.exerciseDbApi

    fun getWorkoutSuggestions(): Flow<List<ExerciseDbModel>> = flow {
        try {
            Log.d(TAG, "ExerciseDB API request started: getExercises")
            val response = exerciseDbApi.getExercises(limit = 50)
            Log.d(TAG, "ExerciseDB API response received: ${response.size} items")
            emit(response)
        } catch (e: Exception) {
            Log.e(TAG, "ExerciseDB API request failed", e)
            throw e
        }
    }
}

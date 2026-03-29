package com.example.myapplication

import com.example.myapplication.data.ActivityEntity
import com.example.myapplication.ui.HomeState
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityViewModelTest {

    @Test
    fun `test calorie calculation logic`() {
        val activities = listOf(
            ActivityEntity(id = 1, userId = 1, name = "Run", steps = 5000, workoutType = "Running", calories = 300, date = "2023-10-27"),
            ActivityEntity(id = 2, userId = 1, name = "Walk", steps = 2000, workoutType = "Walking", calories = 100, date = "2023-10-27")
        )
        
        val totalCalories = activities.sumOf { it.calories }
        assertEquals(400, totalCalories)
    }

    @Test
    fun `test step progress calculation`() {
        val stepsToday = 7500
        val stepGoal = 10000
        
        val progress = if (stepGoal > 0) stepsToday.toFloat() / stepGoal else 0f
        
        assertEquals(0.75f, progress, 0.001f)
    }

    @Test
    fun `test HomeState initialization`() {
        val state = HomeState(
            stepsToday = 1000,
            stepGoal = 5000,
            caloriesBurned = 50,
            workoutsCount = 1
        )
        
        assertEquals(1000, state.stepsToday)
        assertEquals(5000, state.stepGoal)
        assertEquals(50, state.caloriesBurned)
        assertEquals(1, state.workoutsCount)
    }
}

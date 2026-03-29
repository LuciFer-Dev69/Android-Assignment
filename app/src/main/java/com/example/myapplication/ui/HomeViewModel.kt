package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val activities = activityRepository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterLogs = activityRepository.getAllWaterLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepGoal = userPreferencesRepository.stepGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000)

    val waterToday = activityRepository.getTodayWaterSum(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val foodLogs = activityRepository.getFoodLogsByDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allFoodLogs = activityRepository.getAllFoodLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCaloriesConsumed = activityRepository.getTodayTotalCalories(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalProtein = foodLogs.map { logs -> logs.sumOf { it.protein } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCarbs = foodLogs.map { logs -> logs.sumOf { it.carbs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalFats = foodLogs.map { logs -> logs.sumOf { it.fats } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weightHistory = activityRepository.getAllWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val latestWeight = activityRepository.getLatestWeight()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val homeState = combine(activities, stepGoal) { allActivities, goal ->
        val todayActivities = allActivities.filter { it.date == today }
        HomeState(
            stepsToday = todayActivities.sumOf { it.steps },
            stepGoal = goal,
            caloriesBurned = todayActivities.sumOf { it.calories },
            workoutsCount = todayActivities.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    // New Comparative Analytics Logic
    val weeklyStepAverage = activities.map { allActivities ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.time
        
        val lastSevenDaysActivities = allActivities.filter { 
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            date != null && date.after(sevenDaysAgo)
        }
        
        if (lastSevenDaysActivities.isNotEmpty()) {
            val totalSteps = lastSevenDaysActivities.sumOf { it.steps }
            totalSteps / 7
        } else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activityComparisonText = combine(homeState, weeklyStepAverage) { state, avg ->
        when {
            avg == 0 -> "Start logging to see weekly trends!"
            state.stepsToday > avg -> "🚀 You're doing ${state.stepsToday - avg} steps more than your weekly average!"
            state.stepsToday < avg -> "📉 You're ${avg - state.stepsToday} steps behind your weekly average. Keep moving!"
            else -> "✨ You're right on track with your weekly average!"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun addWater(amount: Int) {
        viewModelScope.launch {
            val userId = userPreferencesRepository.userId.firstOrNull() ?: 0
            activityRepository.insertWaterLog(WaterLogEntity(userId = userId, amountMl = amount, date = today))
        }
    }
    
    fun addFood(
        name: String, 
        calories: Int, 
        protein: Double = 0.0, 
        carbs: Double = 0.0, 
        fats: Double = 0.0, 
        mealType: String = "Snack"
    ) {
        viewModelScope.launch {
            activityRepository.insertFoodLog(
                FoodLogEntity(
                    foodName = name, 
                    calories = calories, 
                    protein = protein, 
                    carbs = carbs, 
                    fats = fats, 
                    mealType = mealType,
                    date = today
                )
            )
        }
    }
    
    fun deleteFood(foodLog: FoodLogEntity) {
        viewModelScope.launch {
            activityRepository.deleteFoodLog(foodLog)
        }
    }

    fun logWeight(weight: Double, note: String = "") {
        viewModelScope.launch {
            activityRepository.insertWeightLog(
                WeightLogEntity(weight = weight, date = System.currentTimeMillis(), note = note)
            )
        }
    }
}

data class HomeState(
    val stepsToday: Int = 0,
    val stepGoal: Int = 5000,
    val caloriesBurned: Int = 0,
    val workoutsCount: Int = 0
)

class HomeViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(activityRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel(
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val waterDao: WaterDao,
    private val foodDao: FoodDao
) : ViewModel() {

    val allUsers = userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCount = userDao.getUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val globalActivityStats = activityDao.getAllActivities()
        .map { activities ->
            GlobalActivityStats(
                totalSteps = activities.sumOf { it.steps },
                totalCaloriesBurned = activities.sumOf { it.calories },
                workoutTypeDistribution = activities.groupBy { it.workoutType }.mapValues { it.value.size }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalActivityStats())

    val globalWaterStats = waterDao.getAllWaterLogs()
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            userDao.updateUser(user)
        }
    }
}

data class GlobalActivityStats(
    val totalSteps: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val workoutTypeDistribution: Map<String, Int> = emptyMap()
)

class AdminViewModelFactory(
    private val userDao: UserDao,
    private val activityDao: ActivityDao,
    private val waterDao: WaterDao,
    private val foodDao: FoodDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(userDao, activityDao, waterDao, foodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

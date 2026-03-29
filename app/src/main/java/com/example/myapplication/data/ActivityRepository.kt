package com.example.myapplication.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val waterDao: WaterDao,
    private val foodDao: FoodDao,
    private val weightDao: WeightDao
) {
    private val TAG = "ActivityRepository"

    // Activities
    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()
    
    suspend fun getActivityById(id: Int): ActivityEntity? = activityDao.getActivityById(id)
    
    suspend fun insertActivity(activity: ActivityEntity) {
        Log.d(TAG, "Activity saved: ${activity.name}")
        activityDao.insertActivity(activity)
    }
    
    suspend fun updateActivity(activity: ActivityEntity) {
        Log.d(TAG, "Database updated (Activity): ${activity.name}")
        activityDao.updateActivity(activity)
    }
    
    suspend fun deleteActivity(activity: ActivityEntity) {
        Log.d(TAG, "Activity deleted: ${activity.name}")
        activityDao.deleteActivity(activity)
    }

    // Water
    fun getTodayWaterSum(date: String): Flow<Int?> = waterDao.getTodayWaterSum(date)
    
    fun getAllWaterLogs(): Flow<List<WaterLogEntity>> = waterDao.getAllWaterLogs()
    
    suspend fun insertWaterLog(waterLog: WaterLogEntity) {
        Log.d(TAG, "Water log added: ${waterLog.amountMl}ml")
        waterDao.insertWaterLog(waterLog)
    }

    // Food
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>> = foodDao.getAllFoodLogs()

    fun getFoodLogsByDate(date: String): Flow<List<FoodLogEntity>> = foodDao.getFoodLogsByDate(date)
    
    fun getTodayTotalCalories(date: String): Flow<Int?> = foodDao.getTodayTotalCalories(date)
    
    suspend fun insertFoodLog(foodLog: FoodLogEntity) {
        Log.d(TAG, "Food log added: ${foodLog.foodName}")
        foodDao.insertFoodLog(foodLog)
    }
    
    suspend fun deleteFoodLog(foodLog: FoodLogEntity) {
        Log.d(TAG, "Food log deleted: ${foodLog.foodName}")
        foodDao.deleteFoodLog(foodLog)
    }

    // Weight
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>> = weightDao.getAllWeightLogs()
    
    fun getLatestWeight(): Flow<WeightLogEntity?> = weightDao.getLatestWeight()
    
    suspend fun insertWeightLog(weightLog: WeightLogEntity) {
        Log.d(TAG, "Weight log added: ${weightLog.weight}kg")
        weightDao.insertWeight(weightLog)
    }
}

package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert
    suspend fun insertFoodLog(foodLog: FoodLogEntity)

    @Query("SELECT * FROM food_logs ORDER BY id DESC")
    fun getAllFoodLogs(): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY id DESC")
    fun getFoodLogsByDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT SUM(calories) FROM food_logs WHERE date = :date")
    fun getTodayTotalCalories(date: String): Flow<Int?>

    @Delete
    suspend fun deleteFoodLog(foodLog: FoodLogEntity)
}

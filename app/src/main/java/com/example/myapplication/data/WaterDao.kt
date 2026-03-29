package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert
    suspend fun insertWaterLog(waterLog: WaterLogEntity)

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    fun getTodayWaterSum(date: String): Flow<Int?>

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY id DESC")
    fun getTodayLogs(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs ORDER BY date DESC")
    fun getAllWaterLogs(): Flow<List<WaterLogEntity>>
}

package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int, // Associated with UserEntity.id
    val amountMl: Int,
    val date: String // yyyy-MM-dd
)

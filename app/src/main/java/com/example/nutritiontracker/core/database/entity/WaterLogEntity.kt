package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey val date: String,
    val totalMl: Int,
    val updatedAt: Long,
)

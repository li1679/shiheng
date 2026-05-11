package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey val date: String,
    val weightKg: Double,
    val note: String?,
    val recordedAt: Long,
    val updatedAt: Long,
)

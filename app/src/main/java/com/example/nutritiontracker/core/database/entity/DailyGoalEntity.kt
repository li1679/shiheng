package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey val date: String,
    val proteinGoalGram: Double,
    val fatGoalGram: Double,
    val carbGoalGram: Double,
    val sourceTemplateId: Long?,
    val isFreeDay: Boolean,
    val updatedAt: Long,
)

package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_templates")
data class GoalTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val proteinGoalGram: Double,
    val fatGoalGram: Double,
    val carbGoalGram: Double,
    val createdAt: Long,
    val updatedAt: Long,
)

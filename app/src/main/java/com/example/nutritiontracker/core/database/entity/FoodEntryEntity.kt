package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nutritiontracker.core.model.MealType

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,
    val mealType: MealType,
    val foodId: Long,
    val foodNameSnapshot: String,
    val foodImagePathSnapshot: String?,
    val actualWeightGram: Double,
    val proteinGram: Double,
    val fatGram: Double,
    val carbGram: Double,
    val caloriesKcal: Double,
    val createdAt: Long,
)

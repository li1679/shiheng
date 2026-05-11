package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val imagePath: String?,
    val baseWeightGram: Double,
    val proteinGram: Double,
    val fatGram: Double,
    val carbGram: Double,
    val isFavorite: Boolean = false,
    val lastLoggedAt: Long? = null,
    val logCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

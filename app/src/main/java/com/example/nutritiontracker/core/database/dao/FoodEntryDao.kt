package com.example.nutritiontracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.model.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY createdAt ASC")
    fun getEntriesForDateStream(date: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE date = :date AND mealType = :mealType ORDER BY createdAt ASC")
    fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun getEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getEntriesForDateOnce(date: String): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE date = :date AND mealType = :mealType ORDER BY createdAt ASC")
    suspend fun getEntriesForDateAndMealOnce(date: String, mealType: MealType): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries ORDER BY date ASC, createdAt ASC")
    suspend fun getAllEntriesOnce(): List<FoodEntryEntity>

    @Insert
    suspend fun insertFoodEntry(entry: FoodEntryEntity): Long

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteFoodEntry(id: Long)
}

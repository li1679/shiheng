package com.example.nutritiontracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nutritiontracker.core.database.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY isFavorite DESC, lastLoggedAt DESC, logCount DESC, name COLLATE NOCASE ASC")
    fun getFoodsStream(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFood(id: Long): FoodEntity?

    @Query("SELECT * FROM foods ORDER BY isFavorite DESC, lastLoggedAt DESC, logCount DESC, name COLLATE NOCASE ASC")
    suspend fun getAllFoodsOnce(): List<FoodEntity>

    @Upsert
    suspend fun upsertFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFood(id: Long)

    @Query("UPDATE foods SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE foods SET lastLoggedAt = :loggedAt, logCount = logCount + 1, updatedAt = :loggedAt WHERE id = :id")
    suspend fun markFoodLogged(id: Long, loggedAt: Long)
}

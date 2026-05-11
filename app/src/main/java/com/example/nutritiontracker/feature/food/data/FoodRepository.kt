package com.example.nutritiontracker.feature.food.data

import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.entity.FoodEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Food(
    val id: Long,
    val name: String,
    val imagePath: String?,
    val baseWeightGram: Double,
    val proteinGram: Double,
    val fatGram: Double,
    val carbGram: Double,
    val isFavorite: Boolean = false,
    val lastLoggedAt: Long? = null,
    val logCount: Int = 0,
)

data class FoodInput(
    val name: String,
    val imagePath: String?,
    val baseWeightGram: Double,
    val proteinGram: Double,
    val fatGram: Double,
    val carbGram: Double,
)

interface FoodRepository {
    fun getFoodsStream(): Flow<List<Food>>

    suspend fun saveFood(input: FoodInput)

    suspend fun toggleFavorite(foodId: Long, isFavorite: Boolean)

    suspend fun markFoodLogged(foodId: Long)
}

class DefaultFoodRepository @Inject constructor(
    private val foodDao: FoodDao,
) : FoodRepository {
    override fun getFoodsStream(): Flow<List<Food>> = foodDao.getFoodsStream().map { entities ->
        entities.map { it.toFood() }
    }

    override suspend fun saveFood(input: FoodInput) {
        val now = System.currentTimeMillis()
        foodDao.upsertFood(
            FoodEntity(
                name = input.name,
                imagePath = input.imagePath,
                baseWeightGram = input.baseWeightGram,
                proteinGram = input.proteinGram,
                fatGram = input.fatGram,
                carbGram = input.carbGram,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun toggleFavorite(foodId: Long, isFavorite: Boolean) {
        foodDao.updateFavorite(foodId, isFavorite, System.currentTimeMillis())
    }

    override suspend fun markFoodLogged(foodId: Long) {
        foodDao.markFoodLogged(foodId, System.currentTimeMillis())
    }
}

private fun FoodEntity.toFood(): Food = Food(
    id = id,
    name = name,
    imagePath = imagePath,
    baseWeightGram = baseWeightGram,
    proteinGram = proteinGram,
    fatGram = fatGram,
    carbGram = carbGram,
    isFavorite = isFavorite,
    lastLoggedAt = lastLoggedAt,
    logCount = logCount,
)

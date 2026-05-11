package com.example.nutritiontracker.feature.diary.data

import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.nutrition.MacroValues
import com.example.nutritiontracker.core.nutrition.NutritionCalculator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FoodEntry(
    val id: Long,
    val date: String,
    val mealType: MealType,
    val foodId: Long,
    val foodName: String,
    val foodImagePath: String?,
    val actualWeightGram: Double,
    val macros: MacroValues,
    val caloriesKcal: Double,
)

data class FoodEntryInput(
    val date: String,
    val mealType: MealType,
    val foodId: Long,
    val actualWeightGram: Double,
)

interface DiaryRepository {
    fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntry>>

    suspend fun addFoodEntry(input: FoodEntryInput): Long

    suspend fun deleteFoodEntry(id: Long)

    suspend fun copyMeal(
        fromDate: String,
        fromMealType: MealType,
        toDate: String,
        toMealType: MealType,
    ): Int

    suspend fun copyDay(fromDate: String, toDate: String): Int
}

class DefaultDiaryRepository @Inject constructor(
    private val foodDao: FoodDao,
    private val foodEntryDao: FoodEntryDao,
) : DiaryRepository {
    override fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntry>> =
        foodEntryDao.getEntriesForDateAndMealStream(date, mealType).map { entries ->
            entries.map { it.toFoodEntry() }
        }

    override suspend fun addFoodEntry(input: FoodEntryInput): Long {
        require(input.actualWeightGram > 0.0) { "actualWeightGram must be greater than 0" }
        val food = checkNotNull(foodDao.getFood(input.foodId)) { "Food not found: ${input.foodId}" }
        val macros = NutritionCalculator.scale(
            base = MacroValues(
                proteinGram = food.proteinGram,
                fatGram = food.fatGram,
                carbGram = food.carbGram,
            ),
            baseWeightGram = food.baseWeightGram,
            actualWeightGram = input.actualWeightGram,
        )

        val now = System.currentTimeMillis()
        val entryId = foodEntryDao.insertFoodEntry(
            FoodEntryEntity(
                date = input.date,
                mealType = input.mealType,
                foodId = food.id,
                foodNameSnapshot = food.name,
                foodImagePathSnapshot = food.imagePath,
                actualWeightGram = input.actualWeightGram,
                proteinGram = macros.proteinGram,
                fatGram = macros.fatGram,
                carbGram = macros.carbGram,
                caloriesKcal = NutritionCalculator.calories(macros),
                createdAt = now,
            ),
        )
        foodDao.markFoodLogged(food.id, now)
        return entryId
    }

    override suspend fun deleteFoodEntry(id: Long) {
        foodEntryDao.deleteFoodEntry(id)
    }

    override suspend fun copyMeal(
        fromDate: String,
        fromMealType: MealType,
        toDate: String,
        toMealType: MealType,
    ): Int {
        val sourceEntries = foodEntryDao.getEntriesForDateAndMealOnce(fromDate, fromMealType)
        copyEntries(sourceEntries, toDate) { toMealType }
        return sourceEntries.size
    }

    override suspend fun copyDay(fromDate: String, toDate: String): Int {
        val sourceEntries = foodEntryDao.getEntriesForDateOnce(fromDate)
        copyEntries(sourceEntries, toDate) { it.mealType }
        return sourceEntries.size
    }

    private suspend fun copyEntries(
        sourceEntries: List<FoodEntryEntity>,
        targetDate: String,
        targetMealType: (FoodEntryEntity) -> MealType,
    ) {
        val now = System.currentTimeMillis()
        sourceEntries.forEachIndexed { index, entry ->
            foodEntryDao.insertFoodEntry(
                entry.copy(
                    id = 0L,
                    date = targetDate,
                    mealType = targetMealType(entry),
                    createdAt = now + index,
                ),
            )
            foodDao.markFoodLogged(entry.foodId, now + index)
        }
    }
}

fun FoodEntryEntity.toFoodEntry(): FoodEntry = FoodEntry(
    id = id,
    date = date,
    mealType = mealType,
    foodId = foodId,
    foodName = foodNameSnapshot,
    foodImagePath = foodImagePathSnapshot,
    actualWeightGram = actualWeightGram,
    macros = MacroValues(
        proteinGram = proteinGram,
        fatGram = fatGram,
        carbGram = carbGram,
    ),
    caloriesKcal = caloriesKcal,
)

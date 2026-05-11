package com.example.nutritiontracker.feature.diary

import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.entity.FoodEntity
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.diary.data.DefaultDiaryRepository
import com.example.nutritiontracker.feature.diary.data.FoodEntryInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDiaryRepositoryTest {
    @Test
    fun addFoodEntryStoresMacroAndCalorieSnapshotFromActualWeight() = runTest {
        val foodDao = FakeFoodDao(
            FoodEntity(
                id = 7L,
                name = "米饭",
                imagePath = "rice.jpg",
                baseWeightGram = 500.0,
                proteinGram = 10.0,
                fatGram = 5.0,
                carbGram = 100.0,
                createdAt = 100L,
                updatedAt = 100L,
            ),
        )
        val foodEntryDao = FakeFoodEntryDao()
        val repository = DefaultDiaryRepository(foodDao = foodDao, foodEntryDao = foodEntryDao)

        repository.addFoodEntry(
            FoodEntryInput(
                date = "2026-05-11",
                mealType = MealType.LUNCH,
                foodId = 7L,
                actualWeightGram = 250.0,
            ),
        )

        val savedEntry = foodEntryDao.entries.single()
        assertEquals("2026-05-11", savedEntry.date)
        assertEquals(MealType.LUNCH, savedEntry.mealType)
        assertEquals(7L, savedEntry.foodId)
        assertEquals("米饭", savedEntry.foodNameSnapshot)
        assertEquals("rice.jpg", savedEntry.foodImagePathSnapshot)
        assertEquals(250.0, savedEntry.actualWeightGram, 0.001)
        assertEquals(5.0, savedEntry.proteinGram, 0.001)
        assertEquals(2.5, savedEntry.fatGram, 0.001)
        assertEquals(50.0, savedEntry.carbGram, 0.001)
        assertEquals(242.5, savedEntry.caloriesKcal, 0.001)
    }

    @Test
    fun addFoodEntryMarksFoodAsRecentlyLogged() = runTest {
        val foodDao = FakeFoodDao(
            FoodEntity(
                id = 7L,
                name = "米饭",
                imagePath = "rice.jpg",
                baseWeightGram = 500.0,
                proteinGram = 10.0,
                fatGram = 5.0,
                carbGram = 100.0,
                createdAt = 100L,
                updatedAt = 100L,
            ),
        )
        val repository = DefaultDiaryRepository(foodDao = foodDao, foodEntryDao = FakeFoodEntryDao())

        repository.addFoodEntry(
            FoodEntryInput(
                date = "2026-05-11",
                mealType = MealType.LUNCH,
                foodId = 7L,
                actualWeightGram = 250.0,
            ),
        )

        assertEquals(7L, foodDao.loggedFoodIds.single())
    }

    @Test
    fun copyMealDuplicatesYesterdayMealIntoCurrentMeal() = runTest {
        val foodDao = FakeFoodDao(food = null)
        val foodEntryDao = FakeFoodEntryDao()
        foodEntryDao.seed(
            FoodEntryEntity(
                id = 21L,
                date = "2026-05-10",
                mealType = MealType.LUNCH,
                foodId = 7L,
                foodNameSnapshot = "米饭",
                foodImagePathSnapshot = null,
                actualWeightGram = 250.0,
                proteinGram = 5.0,
                fatGram = 2.5,
                carbGram = 50.0,
                caloriesKcal = 242.5,
                createdAt = 100L,
            ),
        )
        val repository = DefaultDiaryRepository(foodDao = foodDao, foodEntryDao = foodEntryDao)

        val count = repository.copyMeal(
            fromDate = "2026-05-10",
            fromMealType = MealType.LUNCH,
            toDate = "2026-05-11",
            toMealType = MealType.LUNCH,
        )

        assertEquals(1, count)
        val copied = foodEntryDao.insertedRawEntries.single()
        assertEquals("2026-05-11", copied.date)
        assertEquals(MealType.LUNCH, copied.mealType)
        assertEquals("米饭", copied.foodNameSnapshot)
        assertEquals(0L, copied.id)
        assertEquals(7L, foodDao.loggedFoodIds.single())
    }
}

private class FakeFoodDao(private val food: FoodEntity?) : FoodDao {
    val loggedFoodIds = mutableListOf<Long>()

    override fun getFoodsStream(): Flow<List<FoodEntity>> = MutableStateFlow(listOfNotNull(food))

    override suspend fun getFood(id: Long): FoodEntity? = food?.takeIf { it.id == id }

    override suspend fun upsertFood(food: FoodEntity) = Unit

    override suspend fun deleteFood(id: Long) = Unit

    override suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long) = Unit

    override suspend fun markFoodLogged(id: Long, loggedAt: Long) {
        loggedFoodIds += id
    }

    override suspend fun getAllFoodsOnce(): List<FoodEntity> = listOfNotNull(food)
}

private class FakeFoodEntryDao : FoodEntryDao {
    val entries = mutableListOf<FoodEntryEntity>()
    val insertedRawEntries = mutableListOf<FoodEntryEntity>()
    private val entriesStream = MutableStateFlow<List<FoodEntryEntity>>(emptyList())

    fun seed(entry: FoodEntryEntity) {
        entries += entry
        entriesStream.value = entries.toList()
    }

    override fun getEntriesForDateStream(date: String): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entries.filter { it.date == date })

    override fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entries.filter { it.date == date && it.mealType == mealType })

    override fun getEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entries.filter { it.date in startDate..endDate })

    override suspend fun getEntriesForDateOnce(date: String): List<FoodEntryEntity> =
        entries.filter { it.date == date }

    override suspend fun getEntriesForDateAndMealOnce(date: String, mealType: MealType): List<FoodEntryEntity> =
        entries.filter { it.date == date && it.mealType == mealType }

    override suspend fun insertFoodEntry(entry: FoodEntryEntity): Long {
        insertedRawEntries += entry
        val saved = entry.copy(id = (entries.size + 1).toLong())
        entries += saved
        entriesStream.value = entries.toList()
        return saved.id
    }

    override suspend fun deleteFoodEntry(id: Long) {
        entries.removeAll { it.id == id }
        entriesStream.value = entries.toList()
    }

    override suspend fun getAllEntriesOnce(): List<FoodEntryEntity> = entries.toList()
}

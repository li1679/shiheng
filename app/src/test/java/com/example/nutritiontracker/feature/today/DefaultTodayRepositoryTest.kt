package com.example.nutritiontracker.feature.today

import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.today.data.DefaultTodayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTodayRepositoryTest {
    @Test
    fun summarySumsEntriesAndKeepsAllSixMealGroups() = runTest {
        val entryDao = FakeTodayFoodEntryDao(
            listOf(
                entry(id = 1L, mealType = MealType.BREAKFAST, protein = 10.0, fat = 4.0, carb = 20.0, calories = 156.0),
                entry(id = 2L, mealType = MealType.LUNCH, protein = 15.0, fat = 5.0, carb = 40.0, calories = 265.0),
                entry(id = 3L, mealType = MealType.EVENING_SNACK, protein = 5.0, fat = 2.0, carb = 5.0, calories = 58.0),
            ),
        )
        val repository = DefaultTodayRepository(
            foodEntryDao = entryDao,
            goalDao = FakeGoalDao(
                DailyGoalEntity(
                    date = "2026-05-11",
                    proteinGoalGram = 180.0,
                    fatGoalGram = 60.0,
                    carbGoalGram = 220.0,
                    sourceTemplateId = null,
                    isFreeDay = false,
                    updatedAt = 100L,
                ),
            ),
            waterDao = FakeWaterDao(totalMl = 750),
            weightDao = FakeWeightDao(WeightLogEntity(date = "2026-05-11", weightKg = 72.4, note = "空腹", recordedAt = 100L, updatedAt = 100L)),
        )

        val summary = repository.getSummaryStream("2026-05-11").first()

        assertEquals(30.0, summary.totalMacros.proteinGram, 0.001)
        assertEquals(11.0, summary.totalMacros.fatGram, 0.001)
        assertEquals(65.0, summary.totalMacros.carbGram, 0.001)
        assertEquals(479.0, summary.caloriesKcal, 0.001)
        assertEquals(6, summary.meals.size)
        assertEquals(1, summary.meals.single { it.mealType == MealType.LUNCH }.entries.size)
        assertEquals(0, summary.meals.single { it.mealType == MealType.MORNING_SNACK }.entries.size)
        assertEquals(180.0, summary.dailyGoal?.proteinGoalGram ?: 0.0, 0.001)
        assertEquals(750, summary.waterLog?.totalMl)
        assertEquals("空腹", summary.weightLog?.note)
    }
}

private fun entry(
    id: Long,
    mealType: MealType,
    protein: Double,
    fat: Double,
    carb: Double,
    calories: Double,
): FoodEntryEntity = FoodEntryEntity(
    id = id,
    date = "2026-05-11",
    mealType = mealType,
    foodId = id,
    foodNameSnapshot = "食物$id",
    foodImagePathSnapshot = null,
    actualWeightGram = 100.0,
    proteinGram = protein,
    fatGram = fat,
    carbGram = carb,
    caloriesKcal = calories,
    createdAt = id,
)

private class FakeTodayFoodEntryDao(entries: List<FoodEntryEntity>) : FoodEntryDao {
    private val entriesStream = MutableStateFlow(entries)

    override fun getEntriesForDateStream(date: String): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entriesStream.value.filter { it.date == date })

    override fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entriesStream.value.filter { it.date == date && it.mealType == mealType })

    override fun getEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entriesStream.value.filter { it.date in startDate..endDate })

    override suspend fun insertFoodEntry(entry: FoodEntryEntity): Long = error("Not used")

    override suspend fun deleteFoodEntry(id: Long) = Unit

    override suspend fun getEntriesForDateOnce(date: String): List<FoodEntryEntity> =
        entriesStream.value.filter { it.date == date }

    override suspend fun getEntriesForDateAndMealOnce(date: String, mealType: MealType): List<FoodEntryEntity> =
        entriesStream.value.filter { it.date == date && it.mealType == mealType }

    override suspend fun getAllEntriesOnce(): List<FoodEntryEntity> = entriesStream.value
}

private class FakeGoalDao(private val goal: DailyGoalEntity?) : GoalDao {
    override fun getGoalTemplatesStream(): Flow<List<GoalTemplateEntity>> = MutableStateFlow(emptyList())

    override fun getDailyGoalStream(date: String): Flow<DailyGoalEntity?> =
        MutableStateFlow(goal?.takeIf { it.date == date })

    override fun getDailyGoalsBetweenDatesStream(startDate: String, endDate: String): Flow<List<DailyGoalEntity>> =
        MutableStateFlow(listOfNotNull(goal).filter { it.date in startDate..endDate })

    override suspend fun upsertGoalTemplate(template: GoalTemplateEntity) = Unit

    override suspend fun upsertDailyGoal(goal: DailyGoalEntity) = Unit

    override suspend fun deleteGoalTemplate(id: Long) = Unit

    override suspend fun getAllGoalTemplatesOnce(): List<GoalTemplateEntity> = emptyList()

    override suspend fun getAllDailyGoalsOnce(): List<DailyGoalEntity> = listOfNotNull(goal)
}

private class FakeWaterDao(private val totalMl: Int) : WaterDao {
    override fun getWaterEntriesStream(date: String): Flow<List<WaterEntryEntity>> =
        MutableStateFlow(emptyList())

    override fun getWaterEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<WaterEntryEntity>> =
        MutableStateFlow(emptyList())

    override fun getWaterTotalMlStream(date: String): Flow<Int> =
        MutableStateFlow(totalMl)

    override suspend fun insertWaterEntry(entry: WaterEntryEntity): Long = error("Not used")

    override suspend fun deleteWaterEntry(id: Long) = Unit

    override suspend fun getAllWaterEntriesOnce(): List<WaterEntryEntity> = emptyList()
}

private class FakeWeightDao(private val weightLog: WeightLogEntity?) : WeightDao {
    override fun getWeightLogsStream(): Flow<List<WeightLogEntity>> =
        MutableStateFlow(listOfNotNull(weightLog))

    override fun getWeightLogsBetweenDatesStream(startDate: String, endDate: String): Flow<List<WeightLogEntity>> =
        MutableStateFlow(listOfNotNull(weightLog).filter { it.date in startDate..endDate })

    override fun getWeightLogStream(date: String): Flow<WeightLogEntity?> =
        MutableStateFlow(weightLog?.takeIf { it.date == date })

    override suspend fun upsertWeightLog(log: WeightLogEntity) = Unit

    override suspend fun getAllWeightLogsOnce(): List<WeightLogEntity> = listOfNotNull(weightLog)
}

package com.example.nutritiontracker.feature.trends

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
import com.example.nutritiontracker.feature.trends.data.DefaultTrendsRepository
import com.example.nutritiontracker.feature.trends.data.TrendsRange
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTrendsRepositoryTest {
    @Test
    fun weeklySummaryAggregatesNutritionWaterAndWeight() = runTest {
        val repository = DefaultTrendsRepository(
            foodEntryDao = FakeTrendFoodEntryDao(
                listOf(
                    entry(date = "2026-05-11", calories = 235.0, protein = 8.0, fat = 22.0, carb = 0.0),
                    entry(date = "2026-05-12", calories = 235.0, protein = 8.0, fat = 22.0, carb = 0.0),
                    entry(date = "2026-05-13", calories = 235.0, protein = 8.0, fat = 22.0, carb = 0.0),
                ),
            ),
            waterDao = FakeTrendWaterDao(
                listOf(
                    WaterEntryEntity(id = 1L, date = "2026-05-11", amountMl = 250, recordedAt = 100L),
                    WaterEntryEntity(id = 2L, date = "2026-05-11", amountMl = 150, recordedAt = 200L),
                ),
            ),
            goalDao = FakeTrendGoalDao(
                listOf(
                    goal(date = "2026-05-11", protein = 16.0, fat = 44.0, carb = 100.0),
                    goal(date = "2026-05-12", protein = 16.0, fat = 44.0, carb = 100.0),
                ),
            ),
            weightDao = FakeTrendWeightDao(
                listOf(
                    WeightLogEntity(date = "2026-05-11", weightKg = 80.1, note = "复称", recordedAt = 100L, updatedAt = 100L),
                    WeightLogEntity(date = "2026-05-13", weightKg = 79.6, note = null, recordedAt = 200L, updatedAt = 200L),
                ),
            ),
        )

        val summary = repository.getTrendsStream(
            range = TrendsRange.WEEK,
            startDate = LocalDate.parse("2026-05-11"),
            endDate = LocalDate.parse("2026-05-17"),
        ).first()

        assertEquals("2026-05-11", summary.startDate.toString())
        assertEquals("2026-05-17", summary.endDate.toString())
        assertEquals(7, summary.days.size)
        assertEquals(705.0, summary.totalCaloriesKcal, 0.001)
        assertEquals(235.0, summary.averageCaloriesKcal, 0.001)
        assertEquals(8.0, summary.averageProteinGram, 0.001)
        assertEquals(0.0, summary.averageCarbGram, 0.001)
        assertEquals(22.0, summary.averageFatGram, 0.001)
        assertEquals(400, summary.totalWaterMl)
        assertEquals(2, summary.weightLogs.size)
        assertEquals(80.1, summary.weightLogs.first().weightKg, 0.001)
        assertEquals(-0.5, summary.weightChangeKg!!, 0.001)
        assertEquals(3, summary.loggedNutritionDayCount)
        assertEquals(2, summary.goalDayCount)
        assertEquals(33.333, summary.averageMacroCompletionPercent, 0.001)
        assertEquals(235.0, summary.days.first { it.date.toString() == "2026-05-11" }.caloriesKcal, 0.001)
        assertEquals(0.0, summary.days.first { it.date.toString() == "2026-05-14" }.caloriesKcal, 0.001)
    }

    @Test
    fun customSummaryUsesExactStartAndEndDates() = runTest {
        val repository = DefaultTrendsRepository(
            foodEntryDao = FakeTrendFoodEntryDao(
                listOf(
                    entry(date = "2026-05-11", calories = 100.0, protein = 1.0, fat = 1.0, carb = 1.0),
                    entry(date = "2026-05-12", calories = 200.0, protein = 2.0, fat = 2.0, carb = 2.0),
                    entry(date = "2026-05-13", calories = 300.0, protein = 3.0, fat = 3.0, carb = 3.0),
                ),
            ),
            waterDao = FakeTrendWaterDao(
                listOf(
                    WaterEntryEntity(id = 1L, date = "2026-05-12", amountMl = 250, recordedAt = 100L),
                    WaterEntryEntity(id = 2L, date = "2026-05-13", amountMl = 500, recordedAt = 200L),
                ),
            ),
            goalDao = FakeTrendGoalDao(emptyList()),
            weightDao = FakeTrendWeightDao(emptyList()),
        )

        val summary = repository.getTrendsStream(
            range = TrendsRange.CUSTOM,
            startDate = LocalDate.parse("2026-05-12"),
            endDate = LocalDate.parse("2026-05-13"),
        ).first()

        assertEquals(TrendsRange.CUSTOM, summary.range)
        assertEquals("2026-05-12", summary.startDate.toString())
        assertEquals("2026-05-13", summary.endDate.toString())
        assertEquals(2, summary.days.size)
        assertEquals(500.0, summary.totalCaloriesKcal, 0.001)
        assertEquals(750, summary.totalWaterMl)
        assertEquals(200.0, summary.days.first { it.date.toString() == "2026-05-12" }.caloriesKcal, 0.001)
    }
}

private fun goal(
    date: String,
    protein: Double,
    fat: Double,
    carb: Double,
): DailyGoalEntity = DailyGoalEntity(
    date = date,
    proteinGoalGram = protein,
    fatGoalGram = fat,
    carbGoalGram = carb,
    sourceTemplateId = null,
    isFreeDay = false,
    updatedAt = 100L,
)

private fun entry(
    date: String,
    calories: Double,
    protein: Double,
    fat: Double,
    carb: Double,
): FoodEntryEntity = FoodEntryEntity(
    id = date.takeLast(2).toLong(),
    date = date,
    mealType = MealType.LUNCH,
    foodId = 1L,
    foodNameSnapshot = "测试食物",
    foodImagePathSnapshot = null,
    actualWeightGram = 100.0,
    proteinGram = protein,
    fatGram = fat,
    carbGram = carb,
    caloriesKcal = calories,
    createdAt = 100L,
)

private class FakeTrendFoodEntryDao(
    private val entries: List<FoodEntryEntity>,
) : FoodEntryDao {
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

    override suspend fun getAllEntriesOnce(): List<FoodEntryEntity> = entries

    override suspend fun insertFoodEntry(entry: FoodEntryEntity): Long = error("Not used")

    override suspend fun deleteFoodEntry(id: Long) = Unit
}

private class FakeTrendWaterDao(
    private val entries: List<WaterEntryEntity>,
) : WaterDao {
    override fun getWaterEntriesStream(date: String): Flow<List<WaterEntryEntity>> =
        MutableStateFlow(entries.filter { it.date == date })

    override fun getWaterEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<WaterEntryEntity>> =
        MutableStateFlow(entries.filter { it.date in startDate..endDate })

    override fun getWaterTotalMlStream(date: String): Flow<Int> =
        MutableStateFlow(entries.filter { it.date == date }.sumOf { it.amountMl })

    override suspend fun insertWaterEntry(entry: WaterEntryEntity): Long = error("Not used")

    override suspend fun deleteWaterEntry(id: Long) = Unit

    override suspend fun getAllWaterEntriesOnce(): List<WaterEntryEntity> = entries
}

private class FakeTrendGoalDao(
    private val goals: List<DailyGoalEntity>,
) : GoalDao {
    override fun getGoalTemplatesStream(): Flow<List<GoalTemplateEntity>> = MutableStateFlow(emptyList())

    override fun getDailyGoalStream(date: String): Flow<DailyGoalEntity?> =
        MutableStateFlow(goals.firstOrNull { it.date == date })

    override fun getDailyGoalsBetweenDatesStream(startDate: String, endDate: String): Flow<List<DailyGoalEntity>> =
        MutableStateFlow(goals.filter { it.date in startDate..endDate })

    override suspend fun upsertGoalTemplate(template: GoalTemplateEntity) = Unit

    override suspend fun upsertDailyGoal(goal: DailyGoalEntity) = Unit

    override suspend fun deleteGoalTemplate(id: Long) = Unit

    override suspend fun getAllGoalTemplatesOnce(): List<GoalTemplateEntity> = emptyList()

    override suspend fun getAllDailyGoalsOnce(): List<DailyGoalEntity> = goals
}

private class FakeTrendWeightDao(
    private val logs: List<WeightLogEntity>,
) : WeightDao {
    override fun getWeightLogsStream(): Flow<List<WeightLogEntity>> =
        MutableStateFlow(logs)

    override fun getWeightLogsBetweenDatesStream(startDate: String, endDate: String): Flow<List<WeightLogEntity>> =
        MutableStateFlow(logs.filter { it.date in startDate..endDate })

    override fun getWeightLogStream(date: String): Flow<WeightLogEntity?> =
        MutableStateFlow(logs.firstOrNull { it.date == date })

    override suspend fun upsertWeightLog(log: WeightLogEntity) = Unit

    override suspend fun getAllWeightLogsOnce(): List<WeightLogEntity> = logs
}

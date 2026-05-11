package com.example.nutritiontracker.feature.backup

import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.FoodEntity
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import com.example.nutritiontracker.core.datastore.UserPreferences
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.backup.data.DefaultBackupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultBackupRepositoryTest {
    @Test
    fun exportSnapshotContainsAllLocalDataGroups() = runTest {
        val repository = DefaultBackupRepository(
            userPreferencesRepository = FakePreferencesRepository(),
            foodDao = FakeBackupFoodDao(),
            foodEntryDao = FakeBackupFoodEntryDao(),
            goalDao = FakeBackupGoalDao(),
            waterDao = FakeBackupWaterDao(),
            weightDao = FakeBackupWeightDao(),
        )

        val text = repository.exportSnapshot()

        assertTrue(text.contains("NUTRITION_TRACKER_BACKUP_V1"))
        assertTrue(text.contains("defaultFoodBaseWeightGram=500.0"))
        assertTrue(text.contains("鸡胸肉"))
        assertTrue(text.contains("food_entries"))
        assertTrue(text.contains("goal_templates"))
        assertTrue(text.contains("daily_goals"))
        assertTrue(text.contains("water_entries"))
        assertTrue(text.contains("weight_logs"))
        assertTrue(text.contains("空腹"))
    }
}

private class FakePreferencesRepository : UserPreferencesRepository {
    override val userPreferencesStream: Flow<UserPreferences> = MutableStateFlow(UserPreferences())

    override suspend fun updatePreferences(preferences: UserPreferences) = Unit
}

private class FakeBackupFoodDao : FoodDao {
    private val foods = listOf(
        FoodEntity(
            id = 1L,
            name = "鸡胸肉",
            imagePath = null,
            baseWeightGram = 500.0,
            proteinGram = 110.0,
            fatGram = 9.0,
            carbGram = 0.0,
            isFavorite = true,
            lastLoggedAt = 100L,
            logCount = 2,
            createdAt = 1L,
            updatedAt = 2L,
        ),
    )

    override fun getFoodsStream(): Flow<List<FoodEntity>> = MutableStateFlow(foods)

    override suspend fun getFood(id: Long): FoodEntity? = foods.firstOrNull { it.id == id }

    override suspend fun getAllFoodsOnce(): List<FoodEntity> = foods

    override suspend fun upsertFood(food: FoodEntity) = Unit

    override suspend fun deleteFood(id: Long) = Unit

    override suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long) = Unit

    override suspend fun markFoodLogged(id: Long, loggedAt: Long) = Unit
}

private class FakeBackupFoodEntryDao : FoodEntryDao {
    private val entries = listOf(
        FoodEntryEntity(
            id = 1L,
            date = "2026-05-11",
            mealType = MealType.LUNCH,
            foodId = 1L,
            foodNameSnapshot = "鸡胸肉",
            foodImagePathSnapshot = null,
            actualWeightGram = 250.0,
            proteinGram = 55.0,
            fatGram = 4.5,
            carbGram = 0.0,
            caloriesKcal = 260.5,
            createdAt = 100L,
        ),
    )

    override fun getEntriesForDateStream(date: String): Flow<List<FoodEntryEntity>> = MutableStateFlow(entries.filter { it.date == date })

    override fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entries.filter { it.date == date && it.mealType == mealType })

    override fun getEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<FoodEntryEntity>> =
        MutableStateFlow(entries.filter { it.date in startDate..endDate })

    override suspend fun getEntriesForDateOnce(date: String): List<FoodEntryEntity> = entries.filter { it.date == date }

    override suspend fun getEntriesForDateAndMealOnce(date: String, mealType: MealType): List<FoodEntryEntity> =
        entries.filter { it.date == date && it.mealType == mealType }

    override suspend fun getAllEntriesOnce(): List<FoodEntryEntity> = entries

    override suspend fun insertFoodEntry(entry: FoodEntryEntity): Long = 1L

    override suspend fun deleteFoodEntry(id: Long) = Unit
}

private class FakeBackupGoalDao : GoalDao {
    private val templates = listOf(
        GoalTemplateEntity(
            id = 1L,
            name = "训练日",
            proteinGoalGram = 180.0,
            fatGoalGram = 60.0,
            carbGoalGram = 260.0,
            createdAt = 1L,
            updatedAt = 2L,
        ),
    )
    private val goals = listOf(
        DailyGoalEntity(
            date = "2026-05-11",
            proteinGoalGram = 180.0,
            fatGoalGram = 60.0,
            carbGoalGram = 260.0,
            sourceTemplateId = 1L,
            isFreeDay = false,
            updatedAt = 2L,
        ),
    )

    override fun getGoalTemplatesStream(): Flow<List<GoalTemplateEntity>> = MutableStateFlow(templates)

    override fun getDailyGoalStream(date: String): Flow<DailyGoalEntity?> = MutableStateFlow(goals.firstOrNull { it.date == date })

    override fun getDailyGoalsBetweenDatesStream(startDate: String, endDate: String): Flow<List<DailyGoalEntity>> =
        MutableStateFlow(goals.filter { it.date in startDate..endDate })

    override suspend fun getAllGoalTemplatesOnce(): List<GoalTemplateEntity> = templates

    override suspend fun getAllDailyGoalsOnce(): List<DailyGoalEntity> = goals

    override suspend fun upsertGoalTemplate(template: GoalTemplateEntity) = Unit

    override suspend fun upsertDailyGoal(goal: DailyGoalEntity) = Unit

    override suspend fun deleteGoalTemplate(id: Long) = Unit
}

private class FakeBackupWaterDao : WaterDao {
    private val entries = listOf(WaterEntryEntity(id = 1L, date = "2026-05-11", amountMl = 500, recordedAt = 100L))

    override fun getWaterEntriesStream(date: String): Flow<List<WaterEntryEntity>> = MutableStateFlow(entries.filter { it.date == date })

    override fun getWaterEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<WaterEntryEntity>> =
        MutableStateFlow(entries.filter { it.date in startDate..endDate })

    override fun getWaterTotalMlStream(date: String): Flow<Int> = MutableStateFlow(entries.filter { it.date == date }.sumOf { it.amountMl })

    override suspend fun getAllWaterEntriesOnce(): List<WaterEntryEntity> = entries

    override suspend fun insertWaterEntry(entry: WaterEntryEntity): Long = 1L

    override suspend fun deleteWaterEntry(id: Long) = Unit
}

private class FakeBackupWeightDao : WeightDao {
    private val logs = listOf(WeightLogEntity(date = "2026-05-11", weightKg = 80.1, note = "空腹", recordedAt = 100L, updatedAt = 100L))

    override fun getWeightLogsStream(): Flow<List<WeightLogEntity>> = MutableStateFlow(logs)

    override fun getWeightLogsBetweenDatesStream(startDate: String, endDate: String): Flow<List<WeightLogEntity>> =
        MutableStateFlow(logs.filter { it.date in startDate..endDate })

    override fun getWeightLogStream(date: String): Flow<WeightLogEntity?> = MutableStateFlow(logs.firstOrNull { it.date == date })

    override suspend fun getAllWeightLogsOnce(): List<WeightLogEntity> = logs

    override suspend fun upsertWeightLog(log: WeightLogEntity) = Unit
}

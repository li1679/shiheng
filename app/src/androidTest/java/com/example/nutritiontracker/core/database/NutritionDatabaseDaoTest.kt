package com.example.nutritiontracker.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.FoodEntity
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import com.example.nutritiontracker.core.model.MealType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NutritionDatabaseDaoTest {
    private lateinit var database: NutritionDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NutritionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun foodDaoUpsertsAndStreamsFoods() = runTest {
        database.foodDao().upsertFood(
            FoodEntity(
                id = 1L,
                name = "鸡胸肉",
                imagePath = null,
                baseWeightGram = 500.0,
                proteinGram = 110.0,
                fatGram = 9.0,
                carbGram = 0.0,
                createdAt = 100L,
                updatedAt = 100L,
            ),
        )

        val foods = database.foodDao().getFoodsStream().first()

        assertEquals(1, foods.size)
        assertEquals("鸡胸肉", foods.single().name)
    }

    @Test
    fun foodEntryDaoStreamsEntriesForDateAndMeal() = runTest {
        database.foodEntryDao().insertFoodEntry(
            FoodEntryEntity(
                id = 1L,
                date = "2026-05-11",
                mealType = MealType.LUNCH,
                foodId = 1L,
                foodNameSnapshot = "米饭",
                foodImagePathSnapshot = null,
                actualWeightGram = 250.0,
                proteinGram = 6.0,
                fatGram = 1.0,
                carbGram = 65.0,
                caloriesKcal = 293.0,
                createdAt = 100L,
            ),
        )

        val entries = database.foodEntryDao().getEntriesForDateAndMealStream("2026-05-11", MealType.LUNCH).first()

        assertEquals(1, entries.size)
        assertEquals(65.0, entries.single().carbGram, 0.001)
    }

    @Test
    fun goalDaoUpsertsTemplateAndDailyGoal() = runTest {
        database.goalDao().upsertGoalTemplate(
            GoalTemplateEntity(
                id = 1L,
                name = "训练日",
                proteinGoalGram = 180.0,
                fatGoalGram = 60.0,
                carbGoalGram = 260.0,
                createdAt = 100L,
                updatedAt = 100L,
            ),
        )
        database.goalDao().upsertDailyGoal(
            DailyGoalEntity(
                date = "2026-05-11",
                proteinGoalGram = 180.0,
                fatGoalGram = 60.0,
                carbGoalGram = 260.0,
                sourceTemplateId = 1L,
                isFreeDay = false,
                updatedAt = 100L,
            ),
        )

        assertEquals("训练日", database.goalDao().getGoalTemplatesStream().first().single().name)
        assertEquals(260.0, database.goalDao().getDailyGoalStream("2026-05-11").first()!!.carbGoalGram, 0.001)
    }

    @Test
    fun waterUsesSeparateEntriesAndWeightUsesOneRowPerDate() = runTest {
        val firstId = database.waterDao().insertWaterEntry(WaterEntryEntity(date = "2026-05-11", amountMl = 250, recordedAt = 100L))
        database.waterDao().insertWaterEntry(WaterEntryEntity(date = "2026-05-11", amountMl = 500, recordedAt = 200L))
        database.weightDao().upsertWeightLog(WeightLogEntity(date = "2026-05-11", weightKg = 80.5, note = "空腹", recordedAt = 100L, updatedAt = 100L))
        database.weightDao().upsertWeightLog(WeightLogEntity(date = "2026-05-11", weightKg = 80.1, note = "复称", recordedAt = 200L, updatedAt = 200L))

        assertEquals(750, database.waterDao().getWaterTotalMlStream("2026-05-11").first())
        assertEquals(listOf(500, 250), database.waterDao().getWaterEntriesStream("2026-05-11").first().map { it.amountMl })
        database.waterDao().deleteWaterEntry(firstId)
        assertEquals(500, database.waterDao().getWaterTotalMlStream("2026-05-11").first())
        assertEquals(80.1, database.weightDao().getWeightLogStream("2026-05-11").first()!!.weightKg, 0.001)
        assertEquals(1, database.weightDao().getWeightLogsStream().first().size)
    }
}

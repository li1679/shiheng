package com.example.nutritiontracker.feature.today.data

import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.nutrition.MacroValues
import com.example.nutritiontracker.feature.diary.data.FoodEntry
import com.example.nutritiontracker.feature.diary.data.toFoodEntry
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class TodaySummary(
    val date: String,
    val totalMacros: MacroValues = MacroValues(0.0, 0.0, 0.0),
    val caloriesKcal: Double = 0.0,
    val meals: List<MealSummary> = MealType.entries.map { MealSummary(mealType = it) },
    val dailyGoal: DailyGoalSummary? = null,
    val waterLog: WaterLogSummary? = null,
    val weightLog: WeightLogSummary? = null,
)

data class MealSummary(
    val mealType: MealType,
    val entries: List<FoodEntry> = emptyList(),
    val totalMacros: MacroValues = MacroValues(0.0, 0.0, 0.0),
    val caloriesKcal: Double = 0.0,
)

data class DailyGoalSummary(
    val date: String,
    val proteinGoalGram: Double,
    val fatGoalGram: Double,
    val carbGoalGram: Double,
    val isFreeDay: Boolean,
)

data class WaterLogSummary(
    val date: String,
    val totalMl: Int,
)

data class WeightLogSummary(
    val date: String,
    val weightKg: Double,
    val note: String?,
)

interface TodayRepository {
    fun getSummaryStream(date: String): Flow<TodaySummary>
}

class DefaultTodayRepository @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val goalDao: GoalDao,
    private val waterDao: WaterDao,
    private val weightDao: WeightDao,
) : TodayRepository {
    override fun getSummaryStream(date: String): Flow<TodaySummary> =
        combine(
            foodEntryDao.getEntriesForDateStream(date),
            goalDao.getDailyGoalStream(date),
            waterDao.getWaterTotalMlStream(date),
            weightDao.getWeightLogStream(date),
        ) { entries, goal, waterTotalMl, weightLog ->
            val foodEntries = entries.map { it.toFoodEntry() }
            val meals = MealType.entries.map { mealType ->
                val mealEntries = foodEntries.filter { it.mealType == mealType }
                MealSummary(
                    mealType = mealType,
                    entries = mealEntries,
                    totalMacros = mealEntries.sumMacros(),
                    caloriesKcal = mealEntries.sumOf { it.caloriesKcal },
                )
            }

            TodaySummary(
                date = date,
                totalMacros = foodEntries.sumMacros(),
                caloriesKcal = foodEntries.sumOf { it.caloriesKcal },
                meals = meals,
                dailyGoal = goal?.toDailyGoalSummary(),
                waterLog = WaterLogSummary(date = date, totalMl = waterTotalMl),
                weightLog = weightLog?.toWeightLogSummary(),
            )
        }
}

private fun List<FoodEntry>.sumMacros(): MacroValues = MacroValues(
    proteinGram = sumOf { it.macros.proteinGram },
    fatGram = sumOf { it.macros.fatGram },
    carbGram = sumOf { it.macros.carbGram },
)

private fun DailyGoalEntity.toDailyGoalSummary(): DailyGoalSummary = DailyGoalSummary(
    date = date,
    proteinGoalGram = proteinGoalGram,
    fatGoalGram = fatGoalGram,
    carbGoalGram = carbGoalGram,
    isFreeDay = isFreeDay,
)

private fun WeightLogEntity.toWeightLogSummary(): WeightLogSummary = WeightLogSummary(
    date = date,
    weightKg = weightKg,
    note = note,
)

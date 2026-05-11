package com.example.nutritiontracker.feature.trends.data

import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import com.example.nutritiontracker.feature.weight.data.WeightLog
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

enum class TrendsRange(val label: String) {
    WEEK("本周"),
    MONTH("本月"),
    YEAR("今年"),
    CUSTOM("自选"),
}

data class NutritionTrendDay(
    val date: LocalDate,
    val caloriesKcal: Double = 0.0,
    val proteinGram: Double = 0.0,
    val fatGram: Double = 0.0,
    val carbGram: Double = 0.0,
    val waterMl: Int = 0,
    val proteinGoalGram: Double = 0.0,
    val fatGoalGram: Double = 0.0,
    val carbGoalGram: Double = 0.0,
) {
    val hasNutrition: Boolean = caloriesKcal > 0.0 || proteinGram > 0.0 || fatGram > 0.0 || carbGram > 0.0
    val hasGoal: Boolean = proteinGoalGram > 0.0 || fatGoalGram > 0.0 || carbGoalGram > 0.0
    val macroCompletionPercent: Double? = if (hasGoal) {
        val ratios = buildList {
            if (proteinGoalGram > 0.0) add((proteinGram / proteinGoalGram).coerceAtMost(1.0))
            if (fatGoalGram > 0.0) add((fatGram / fatGoalGram).coerceAtMost(1.0))
            if (carbGoalGram > 0.0) add((carbGram / carbGoalGram).coerceAtMost(1.0))
        }
        if (ratios.isEmpty()) null else ratios.average() * 100.0
    } else null
}

data class TrendsSummary(
    val range: TrendsRange,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<NutritionTrendDay> = emptyList(),
    val weightLogs: List<WeightLog> = emptyList(),
) {
    val totalCaloriesKcal: Double = days.sumOf { it.caloriesKcal }
    val totalWaterMl: Int = days.sumOf { it.waterMl }
    val loggedNutritionDayCount: Int = days.count { it.hasNutrition }
    val goalDayCount: Int = days.count { it.hasGoal }

    private val nutritionDays: List<NutritionTrendDay> = days.filter { it.hasNutrition }
    private val nutritionDayCount: Int = nutritionDays.size.coerceAtLeast(1)

    val averageCaloriesKcal: Double = nutritionDays.sumOf { it.caloriesKcal } / nutritionDayCount
    val averageProteinGram: Double = nutritionDays.sumOf { it.proteinGram } / nutritionDayCount
    val averageFatGram: Double = nutritionDays.sumOf { it.fatGram } / nutritionDayCount
    val averageCarbGram: Double = nutritionDays.sumOf { it.carbGram } / nutritionDayCount
    val averageMacroCompletionPercent: Double = days.mapNotNull { it.macroCompletionPercent }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
    val weightChangeKg: Double? = weightLogs.minByOrNull { it.date }?.weightKg?.let { first ->
        weightLogs.maxByOrNull { it.date }?.weightKg?.let { last -> last - first }
    }
}

interface TrendsRepository {
    fun getTrendsStream(range: TrendsRange, startDate: LocalDate, endDate: LocalDate): Flow<TrendsSummary>
}

class DefaultTrendsRepository @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val waterDao: WaterDao,
    private val goalDao: GoalDao,
    private val weightDao: WeightDao,
) : TrendsRepository {
    override fun getTrendsStream(range: TrendsRange, startDate: LocalDate, endDate: LocalDate): Flow<TrendsSummary> {
        val startDateText = startDate.toString()
        val endDateText = endDate.toString()

        return combine(
            foodEntryDao.getEntriesBetweenDatesStream(startDateText, endDateText),
            waterDao.getWaterEntriesBetweenDatesStream(startDateText, endDateText),
            goalDao.getDailyGoalsBetweenDatesStream(startDateText, endDateText),
            weightDao.getWeightLogsBetweenDatesStream(startDateText, endDateText),
        ) { foodEntries, waterEntries, goals, weightLogs ->
            val goalsByDate = goals.associateBy { it.date }
            TrendsSummary(
                range = range,
                startDate = startDate,
                endDate = endDate,
                days = startDate.datesUntilInclusive(endDate).map { date ->
                    val dateText = date.toString()
                    val foodsForDate = foodEntries.filter { it.date == dateText }
                    val waterForDate = waterEntries.filter { it.date == dateText }
                    val goalForDate = goalsByDate[dateText]
                    NutritionTrendDay(
                        date = date,
                        caloriesKcal = foodsForDate.sumOf { it.caloriesKcal },
                        proteinGram = foodsForDate.sumOf { it.proteinGram },
                        fatGram = foodsForDate.sumOf { it.fatGram },
                        carbGram = foodsForDate.sumOf { it.carbGram },
                        waterMl = waterForDate.sumOf { it.amountMl },
                        proteinGoalGram = goalForDate?.proteinGoalGram ?: 0.0,
                        fatGoalGram = goalForDate?.fatGoalGram ?: 0.0,
                        carbGoalGram = goalForDate?.carbGoalGram ?: 0.0,
                    )
                },
                weightLogs = weightLogs.map { it.toWeightLog() },
            )
        }
    }
}

private fun LocalDate.datesUntilInclusive(endDate: LocalDate): List<LocalDate> =
    generateSequence(this) { date -> date.plusDays(1).takeIf { it <= endDate } }.toList()

private fun WeightLogEntity.toWeightLog(): WeightLog = WeightLog(
    date = date,
    weightKg = weightKg,
    note = note,
)

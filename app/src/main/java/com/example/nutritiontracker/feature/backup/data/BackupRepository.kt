package com.example.nutritiontracker.feature.backup.data

import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

interface BackupRepository {
    suspend fun exportSnapshot(): String
}

class DefaultBackupRepository @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val foodDao: FoodDao,
    private val foodEntryDao: FoodEntryDao,
    private val goalDao: GoalDao,
    private val waterDao: WaterDao,
    private val weightDao: WeightDao,
) : BackupRepository {
    override suspend fun exportSnapshot(): String {
        val preferences = userPreferencesRepository.userPreferencesStream.first()
        val foods = foodDao.getAllFoodsOnce()
        val foodEntries = foodEntryDao.getAllEntriesOnce()
        val templates = goalDao.getAllGoalTemplatesOnce()
        val goals = goalDao.getAllDailyGoalsOnce()
        val waters = waterDao.getAllWaterEntriesOnce()
        val weights = weightDao.getAllWeightLogsOnce()

        return buildString {
            appendLine("NUTRITION_TRACKER_BACKUP_V1")
            appendLine("preferences")
            appendLine("defaultFoodBaseWeightGram=${preferences.defaultFoodBaseWeightGram}")
            appendLine("dailyWaterGoalMl=${preferences.dailyWaterGoalMl}")
            appendLine("quickWaterMlValues=${preferences.quickWaterMlValues.joinToString(",")}")
            appendLine("themeMode=${preferences.themeMode}")
            appendLine("foods")
            foods.forEach { food ->
                appendLine(
                    listOf(
                        food.id,
                        food.name.escape(),
                        food.imagePath.orEmpty().escape(),
                        food.baseWeightGram,
                        food.proteinGram,
                        food.fatGram,
                        food.carbGram,
                        food.isFavorite,
                        food.lastLoggedAt ?: "",
                        food.logCount,
                        food.createdAt,
                        food.updatedAt,
                    ).joinToString("|"),
                )
            }
            appendLine("food_entries")
            foodEntries.forEach { entry ->
                appendLine(
                    listOf(
                        entry.id,
                        entry.date,
                        entry.mealType.name,
                        entry.foodId,
                        entry.foodNameSnapshot.escape(),
                        entry.foodImagePathSnapshot.orEmpty().escape(),
                        entry.actualWeightGram,
                        entry.proteinGram,
                        entry.fatGram,
                        entry.carbGram,
                        entry.caloriesKcal,
                        entry.createdAt,
                    ).joinToString("|"),
                )
            }
            appendLine("goal_templates")
            templates.forEach { template ->
                appendLine(
                    listOf(
                        template.id,
                        template.name.escape(),
                        template.proteinGoalGram,
                        template.fatGoalGram,
                        template.carbGoalGram,
                        template.createdAt,
                        template.updatedAt,
                    ).joinToString("|"),
                )
            }
            appendLine("daily_goals")
            goals.forEach { goal ->
                appendLine(
                    listOf(
                        goal.date,
                        goal.proteinGoalGram,
                        goal.fatGoalGram,
                        goal.carbGoalGram,
                        goal.sourceTemplateId ?: "",
                        goal.isFreeDay,
                        goal.updatedAt,
                    ).joinToString("|"),
                )
            }
            appendLine("water_entries")
            waters.forEach { water ->
                appendLine("${water.id}|${water.date}|${water.amountMl}|${water.recordedAt}")
            }
            appendLine("weight_logs")
            weights.forEach { weight ->
                appendLine(
                    listOf(
                        weight.date,
                        weight.weightKg,
                        weight.note.orEmpty().escape(),
                        weight.recordedAt,
                        weight.updatedAt,
                    ).joinToString("|"),
                )
            }
        }
    }
}

private fun String.escape(): String =
    replace("\\", "\\\\")
        .replace("|", "\\|")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

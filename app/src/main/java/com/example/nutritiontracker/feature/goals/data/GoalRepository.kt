package com.example.nutritiontracker.feature.goals.data

import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import com.example.nutritiontracker.feature.goals.domain.GoalTemplateApplier
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class GoalTemplate(
    val id: Long,
    val name: String,
    val proteinGoalGram: Double,
    val fatGoalGram: Double,
    val carbGoalGram: Double,
)

data class GoalTemplateInput(
    val name: String,
    val proteinGoalGram: Double,
    val fatGoalGram: Double,
    val carbGoalGram: Double,
)

interface GoalRepository {
    fun getTemplatesStream(): Flow<List<GoalTemplate>>

    suspend fun saveTemplate(input: GoalTemplateInput)

    suspend fun deleteTemplate(id: Long)

    suspend fun applyTemplate(
        templateId: Long,
        startDate: LocalDate,
        dayCount: Int,
        skippedDates: Set<LocalDate>,
    )

    suspend fun saveDailyGoal(
        date: LocalDate,
        proteinGoalGram: Double,
        fatGoalGram: Double,
        carbGoalGram: Double,
    )
}

class DefaultGoalRepository @Inject constructor(
    private val goalDao: GoalDao,
) : GoalRepository {
    override fun getTemplatesStream(): Flow<List<GoalTemplate>> =
        goalDao.getGoalTemplatesStream().map { templates -> templates.map { it.toGoalTemplate() } }

    override suspend fun saveTemplate(input: GoalTemplateInput) {
        val now = System.currentTimeMillis()
        goalDao.upsertGoalTemplate(
            GoalTemplateEntity(
                name = input.name,
                proteinGoalGram = input.proteinGoalGram,
                fatGoalGram = input.fatGoalGram,
                carbGoalGram = input.carbGoalGram,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun deleteTemplate(id: Long) {
        goalDao.deleteGoalTemplate(id)
    }

    override suspend fun applyTemplate(
        templateId: Long,
        startDate: LocalDate,
        dayCount: Int,
        skippedDates: Set<LocalDate>,
    ) {
        val template = checkNotNull(
            goalDao.getGoalTemplatesStream().first().firstOrNull { it.id == templateId },
        ) { "Goal template not found: $templateId" }
        val now = System.currentTimeMillis()

        GoalTemplateApplier.datesToApply(startDate, dayCount, skippedDates).forEach { date ->
            goalDao.upsertDailyGoal(
                DailyGoalEntity(
                    date = date.toString(),
                    proteinGoalGram = template.proteinGoalGram,
                    fatGoalGram = template.fatGoalGram,
                    carbGoalGram = template.carbGoalGram,
                    sourceTemplateId = template.id,
                    isFreeDay = false,
                    updatedAt = now,
                ),
            )
        }
    }

    override suspend fun saveDailyGoal(
        date: LocalDate,
        proteinGoalGram: Double,
        fatGoalGram: Double,
        carbGoalGram: Double,
    ) {
        goalDao.upsertDailyGoal(
            DailyGoalEntity(
                date = date.toString(),
                proteinGoalGram = proteinGoalGram,
                fatGoalGram = fatGoalGram,
                carbGoalGram = carbGoalGram,
                sourceTemplateId = null,
                isFreeDay = false,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }
}

private fun GoalTemplateEntity.toGoalTemplate(): GoalTemplate = GoalTemplate(
    id = id,
    name = name,
    proteinGoalGram = proteinGoalGram,
    fatGoalGram = fatGoalGram,
    carbGoalGram = carbGoalGram,
)

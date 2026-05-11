package com.example.nutritiontracker.feature.goals

import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import com.example.nutritiontracker.feature.goals.data.DefaultGoalRepository
import com.example.nutritiontracker.feature.goals.data.GoalTemplateInput
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultGoalRepositoryTest {
    @Test
    fun saveTemplateStoresGoalTemplate() = runTest {
        val goalDao = FakeRepositoryGoalDao()
        val repository = DefaultGoalRepository(goalDao)

        repository.saveTemplate(
            GoalTemplateInput(
                name = "训练日",
                proteinGoalGram = 180.0,
                fatGoalGram = 60.0,
                carbGoalGram = 260.0,
            ),
        )

        val saved = goalDao.templates.value.single()
        assertEquals("训练日", saved.name)
        assertEquals(180.0, saved.proteinGoalGram, 0.001)
        assertEquals(60.0, saved.fatGoalGram, 0.001)
        assertEquals(260.0, saved.carbGoalGram, 0.001)
    }

    @Test
    fun applyTemplateUpsertsDailyGoalsAndSkipsSelectedDates() = runTest {
        val goalDao = FakeRepositoryGoalDao()
        goalDao.templates.value = listOf(
            GoalTemplateEntity(
                id = 9L,
                name = "训练日",
                proteinGoalGram = 180.0,
                fatGoalGram = 60.0,
                carbGoalGram = 260.0,
                createdAt = 100L,
                updatedAt = 100L,
            ),
        )
        val repository = DefaultGoalRepository(goalDao)

        repository.applyTemplate(
            templateId = 9L,
            startDate = LocalDate.of(2026, 5, 11),
            dayCount = 3,
            skippedDates = setOf(LocalDate.of(2026, 5, 12)),
        )

        assertEquals(listOf("2026-05-11", "2026-05-13"), goalDao.dailyGoals.map { it.date })
        assertEquals(9L, goalDao.dailyGoals.single { it.date == "2026-05-11" }.sourceTemplateId)
        assertEquals(260.0, goalDao.dailyGoals.single { it.date == "2026-05-13" }.carbGoalGram, 0.001)
    }

    @Test
    fun saveDailyGoalUpsertsCustomGoalWithoutTemplateSource() = runTest {
        val goalDao = FakeRepositoryGoalDao()
        val repository = DefaultGoalRepository(goalDao)

        repository.saveDailyGoal(
            date = LocalDate.of(2026, 6, 3),
            proteinGoalGram = 135.0,
            fatGoalGram = 45.0,
            carbGoalGram = 210.0,
        )

        val saved = goalDao.dailyGoals.single()
        assertEquals("2026-06-03", saved.date)
        assertEquals(135.0, saved.proteinGoalGram, 0.001)
        assertEquals(45.0, saved.fatGoalGram, 0.001)
        assertEquals(210.0, saved.carbGoalGram, 0.001)
        assertEquals(null, saved.sourceTemplateId)
    }
}

private class FakeRepositoryGoalDao : GoalDao {
    val templates = MutableStateFlow<List<GoalTemplateEntity>>(emptyList())
    val dailyGoals = mutableListOf<DailyGoalEntity>()

    override fun getGoalTemplatesStream(): Flow<List<GoalTemplateEntity>> = templates

    override fun getDailyGoalStream(date: String): Flow<DailyGoalEntity?> =
        MutableStateFlow(dailyGoals.firstOrNull { it.date == date })

    override fun getDailyGoalsBetweenDatesStream(startDate: String, endDate: String): Flow<List<DailyGoalEntity>> =
        MutableStateFlow(dailyGoals.filter { it.date in startDate..endDate })

    override suspend fun upsertGoalTemplate(template: GoalTemplateEntity) {
        val saved = template.copy(id = if (template.id == 0L) templates.value.size + 1L else template.id)
        templates.value = templates.value.filterNot { it.id == saved.id } + saved
    }

    override suspend fun upsertDailyGoal(goal: DailyGoalEntity) {
        dailyGoals.removeAll { it.date == goal.date }
        dailyGoals += goal
    }

    override suspend fun deleteGoalTemplate(id: Long) {
        templates.value = templates.value.filterNot { it.id == id }
    }

    override suspend fun getAllGoalTemplatesOnce(): List<GoalTemplateEntity> = templates.value

    override suspend fun getAllDailyGoalsOnce(): List<DailyGoalEntity> = dailyGoals.toList()
}

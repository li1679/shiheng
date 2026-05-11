package com.example.nutritiontracker.feature.goals

import com.example.nutritiontracker.feature.goals.data.GoalRepository
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.goals.data.GoalTemplateInput
import com.example.nutritiontracker.feature.goals.presentation.GoalsViewModel
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveTemplateStoresValidDraftAndClearsDraft() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        val viewModel = GoalsViewModel(repository)

        viewModel.onTemplateNameChange("训练日")
        viewModel.onProteinChange("180")
        viewModel.onFatChange("60")
        viewModel.onCarbChange("260")
        assertTrue(viewModel.saveTemplate())
        advanceUntilIdle()

        assertEquals("训练日", repository.savedTemplates.single().name)
        assertEquals(260.0, repository.savedTemplates.single().carbGoalGram, 0.001)
        assertEquals("", viewModel.uiState.value.templateDraft.name)
    }

    @Test
    fun saveTemplateRejectsNegativeMacro() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        val viewModel = GoalsViewModel(repository)

        viewModel.onTemplateNameChange("无效")
        viewModel.onProteinChange("-1")
        viewModel.onFatChange("60")
        viewModel.onCarbChange("260")
        assertFalse(viewModel.saveTemplate())
        advanceUntilIdle()

        assertTrue(repository.savedTemplates.isEmpty())
        assertEquals("三大营养素目标不能为负数", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun applyTemplateParsesSkippedDatesAndUsesSelectedTemplate() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        repository.templates.value = listOf(GoalTemplate(id = 8L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0))
        val viewModel = GoalsViewModel(repository)

        viewModel.onTemplateSelected(8L)
        viewModel.onApplyStartDateChange("2026-05-11")
        viewModel.onApplyDayCountChange("3")
        viewModel.onSkippedDatesChange("2026-05-12")
        assertTrue(viewModel.applyTemplate())
        advanceUntilIdle()

        assertEquals(8L, repository.applyCalls.single().templateId)
        assertEquals(LocalDate.of(2026, 5, 11), repository.applyCalls.single().startDate)
        assertEquals(3, repository.applyCalls.single().dayCount)
        assertEquals(setOf(LocalDate.of(2026, 5, 12)), repository.applyCalls.single().skippedDates)
    }

    @Test
    fun togglePreviewDateAddsAndRemovesSkippedDate() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        repository.templates.value = listOf(GoalTemplate(id = 8L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0))
        val viewModel = GoalsViewModel(repository)
        advanceUntilIdle()

        viewModel.onApplyStartDateChange("2026-05-11")
        viewModel.onApplyDayCountChange("3")
        viewModel.toggleSkippedDate(LocalDate.of(2026, 5, 12))
        advanceUntilIdle()

        assertEquals("2026-05-12", viewModel.uiState.value.applyDraft.skippedDates)
        assertEquals(true, viewModel.uiState.value.previewDates.first { it.date == LocalDate.of(2026, 5, 12) }.isSkipped)

        viewModel.toggleSkippedDate(LocalDate.of(2026, 5, 12))
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.applyDraft.skippedDates)
    }
}

private class FakeGoalRepository : GoalRepository {
    val templates = MutableStateFlow<List<GoalTemplate>>(emptyList())
    val savedTemplates = mutableListOf<GoalTemplateInput>()
    val applyCalls = mutableListOf<ApplyCall>()

    override fun getTemplatesStream(): Flow<List<GoalTemplate>> = templates

    override suspend fun saveTemplate(input: GoalTemplateInput) {
        savedTemplates += input
    }

    override suspend fun deleteTemplate(id: Long) = Unit

    override suspend fun applyTemplate(
        templateId: Long,
        startDate: LocalDate,
        dayCount: Int,
        skippedDates: Set<LocalDate>,
    ) {
        applyCalls += ApplyCall(templateId, startDate, dayCount, skippedDates)
    }

    override suspend fun saveDailyGoal(
        date: LocalDate,
        proteinGoalGram: Double,
        fatGoalGram: Double,
        carbGoalGram: Double,
    ) = Unit
}

private data class ApplyCall(
    val templateId: Long,
    val startDate: LocalDate,
    val dayCount: Int,
    val skippedDates: Set<LocalDate>,
)

package com.example.nutritiontracker.feature.today

import com.example.nutritiontracker.feature.goals.data.GoalRepository
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.goals.data.GoalTemplateInput
import com.example.nutritiontracker.feature.today.presentation.TodayGoalQuickApplyViewModel
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
class TodayGoalQuickApplyViewModelTest {
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
    fun firstTemplateIsSelectedByDefaultAndCanChangeSelection() = runTest(dispatcher) {
        val repository = FakeGoalRepository(
            listOf(
                GoalTemplate(id = 1L, name = "休息日", proteinGoalGram = 120.0, fatGoalGram = 50.0, carbGoalGram = 180.0),
                GoalTemplate(id = 2L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0),
            ),
        )
        val viewModel = TodayGoalQuickApplyViewModel(repository)
        advanceUntilIdle()

        assertEquals(1L, viewModel.uiState.value.selectedTemplateId)

        viewModel.selectTemplate(2L)
        advanceUntilIdle()

        assertEquals(2L, viewModel.uiState.value.selectedTemplateId)
    }

    @Test
    fun applySelectedDateAppliesOnlyThatDate() = runTest(dispatcher) {
        val repository = FakeGoalRepository(listOf(GoalTemplate(id = 8L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0)))
        val viewModel = TodayGoalQuickApplyViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.applyToDate("2026-05-22"))
        advanceUntilIdle()

        assertEquals(
            ApplyCall(
                templateId = 8L,
                startDate = LocalDate.parse("2026-05-22"),
                dayCount = 1,
                skippedDates = emptySet(),
            ),
            repository.applyCalls.single(),
        )
    }

    @Test
    fun applyNextThirtyDaysUsesSelectedFutureDateAsStart() = runTest(dispatcher) {
        val repository = FakeGoalRepository(listOf(GoalTemplate(id = 9L, name = "减脂日", proteinGoalGram = 160.0, fatGoalGram = 55.0, carbGoalGram = 190.0)))
        val viewModel = TodayGoalQuickApplyViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.applyForNextThirtyDays("2026-06-01"))
        advanceUntilIdle()

        assertEquals(LocalDate.parse("2026-06-01"), repository.applyCalls.single().startDate)
        assertEquals(30, repository.applyCalls.single().dayCount)
    }

    @Test
    fun applyWithoutTemplatesShowsError() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        val viewModel = TodayGoalQuickApplyViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.applyToDate("2026-05-11"))
        advanceUntilIdle()

        assertEquals("先创建目标模板", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun saveManualGoalStoresCustomValuesForSelectedDate() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        val viewModel = TodayGoalQuickApplyViewModel(repository)

        viewModel.onManualProteinGoalChange("135")
        viewModel.onManualFatGoalChange("45")
        viewModel.onManualCarbGoalChange("210")
        assertTrue(viewModel.saveManualGoal("2026-06-03"))
        advanceUntilIdle()

        assertEquals(
            ManualGoalCall(
                date = LocalDate.parse("2026-06-03"),
                proteinGoalGram = 135.0,
                fatGoalGram = 45.0,
                carbGoalGram = 210.0,
            ),
            repository.manualGoalCalls.single(),
        )
        assertEquals("已保存这一天目标", viewModel.uiState.value.successMessage)
    }

    @Test
    fun saveManualGoalRejectsInvalidValue() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        val viewModel = TodayGoalQuickApplyViewModel(repository)

        viewModel.onManualProteinGoalChange("135")
        viewModel.onManualFatGoalChange("-1")
        viewModel.onManualCarbGoalChange("210")
        assertFalse(viewModel.saveManualGoal("2026-06-03"))
        advanceUntilIdle()

        assertEquals("三大营养素目标不能为负数", viewModel.uiState.value.errorMessage)
        assertTrue(repository.manualGoalCalls.isEmpty())
    }

    @Test
    fun clearMacroGoalStoresZeroForSelectedMacroAndKeepsOtherGoals() = runTest(dispatcher) {
        val repository = FakeGoalRepository()
        val viewModel = TodayGoalQuickApplyViewModel(repository)

        assertTrue(
            viewModel.clearMacroGoal(
                date = "2026-06-03",
                type = com.example.nutritiontracker.feature.today.presentation.MacroGoalType.Protein,
                currentProteinGoal = 135.0,
                currentFatGoal = 45.0,
                currentCarbGoal = 210.0,
            ),
        )
        advanceUntilIdle()

        assertEquals(
            ManualGoalCall(
                date = LocalDate.parse("2026-06-03"),
                proteinGoalGram = 0.0,
                fatGoalGram = 45.0,
                carbGoalGram = 210.0,
            ),
            repository.manualGoalCalls.single(),
        )
    }
}

private class FakeGoalRepository(
    initialTemplates: List<GoalTemplate> = emptyList(),
) : GoalRepository {
    private val templates = MutableStateFlow(initialTemplates)
    val applyCalls = mutableListOf<ApplyCall>()
    val manualGoalCalls = mutableListOf<ManualGoalCall>()

    override fun getTemplatesStream(): Flow<List<GoalTemplate>> = templates

    override suspend fun saveTemplate(input: GoalTemplateInput) = Unit

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
    ) {
        manualGoalCalls += ManualGoalCall(date, proteinGoalGram, fatGoalGram, carbGoalGram)
    }
}

private data class ApplyCall(
    val templateId: Long,
    val startDate: LocalDate,
    val dayCount: Int,
    val skippedDates: Set<LocalDate>,
)

private data class ManualGoalCall(
    val date: LocalDate,
    val proteinGoalGram: Double,
    val fatGoalGram: Double,
    val carbGoalGram: Double,
)

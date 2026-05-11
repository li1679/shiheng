package com.example.nutritiontracker.feature.today

import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.nutrition.MacroValues
import com.example.nutritiontracker.feature.today.data.MealSummary
import com.example.nutritiontracker.feature.today.data.TodayRepository
import com.example.nutritiontracker.feature.today.data.TodaySummary
import com.example.nutritiontracker.feature.today.presentation.TodayViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
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
    fun uiStateShowsSelectedDateSummary() = runTest(dispatcher) {
        val repository = FakeTodayRepository()
        val viewModel = TodayViewModel(repository)

        viewModel.selectDate("2026-05-11")
        repository.setSummary(
            "2026-05-11",
            TodaySummary(
                date = "2026-05-11",
                totalMacros = MacroValues(proteinGram = 30.0, fatGram = 11.0, carbGram = 65.0),
                caloriesKcal = 479.0,
                meals = MealType.entries.map { MealSummary(mealType = it) },
            ),
        )
        advanceUntilIdle()

        assertEquals("2026-05-11", viewModel.uiState.value.summary.date)
        assertEquals(30.0, viewModel.uiState.value.summary.totalMacros.proteinGram, 0.001)
        assertEquals(6, viewModel.uiState.value.summary.meals.size)
    }

    @Test
    fun dateNavigationMovesBySingleDaysAndCanReturnToday() = runTest(dispatcher) {
        val repository = FakeTodayRepository()
        val viewModel = TodayViewModel(repository)

        viewModel.selectDate("2026-05-11")
        viewModel.selectNextDate()
        advanceUntilIdle()
        assertEquals("2026-05-12", viewModel.uiState.value.summary.date)

        viewModel.selectPreviousDate()
        advanceUntilIdle()
        assertEquals("2026-05-11", viewModel.uiState.value.summary.date)

        viewModel.selectToday()
        advanceUntilIdle()
        assertEquals(LocalDate.now().toString(), viewModel.uiState.value.summary.date)
    }
}

private class FakeTodayRepository : TodayRepository {
    private val summaries = mutableMapOf<String, MutableStateFlow<TodaySummary>>()

    override fun getSummaryStream(date: String): Flow<TodaySummary> =
        summaries.getOrPut(date) { MutableStateFlow(TodaySummary(date = date)) }

    fun setSummary(date: String, value: TodaySummary) {
        summaries.getOrPut(date) { MutableStateFlow(TodaySummary(date = date)) }.value = value
    }
}

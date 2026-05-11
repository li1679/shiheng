package com.example.nutritiontracker.feature.trends

import com.example.nutritiontracker.feature.trends.data.TrendsRange
import com.example.nutritiontracker.feature.trends.data.TrendsRepository
import com.example.nutritiontracker.feature.trends.data.TrendsSummary
import com.example.nutritiontracker.feature.trends.presentation.TrendsViewModel
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
class TrendsViewModelTest {
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
    fun defaultRangeIsWeekAndRangeCanSwitchToMonth() = runTest(dispatcher) {
        val repository = FakeTrendsRepository()
        val viewModel = TrendsViewModel(repository)
        advanceUntilIdle()

        assertEquals(TrendsRange.WEEK, viewModel.uiState.value.selectedRange)
        assertEquals(TrendsRange.WEEK, repository.requestedRanges.last())

        viewModel.selectRange(TrendsRange.MONTH)
        advanceUntilIdle()

        assertEquals(TrendsRange.MONTH, viewModel.uiState.value.selectedRange)
        assertEquals(TrendsRange.MONTH, repository.requestedRanges.last())
    }

    @Test
    fun selectedAnchorDateIsSentToRepository() = runTest(dispatcher) {
        val repository = FakeTrendsRepository()
        val viewModel = TrendsViewModel(repository)

        viewModel.selectAnchorDate("2026-05-15")
        advanceUntilIdle()

        assertEquals(LocalDate.parse("2026-05-11"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2026-05-17"), repository.requestedEndDates.last())
        assertEquals(LocalDate.parse("2026-05-15"), viewModel.uiState.value.anchorDate)
    }

    @Test
    fun previousAndNextPeriodMoveBySelectedRange() = runTest(dispatcher) {
        val repository = FakeTrendsRepository()
        val viewModel = TrendsViewModel(repository)

        viewModel.selectAnchorDate("2026-05-15")
        viewModel.selectNextPeriod()
        advanceUntilIdle()
        assertEquals(LocalDate.parse("2026-05-18"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2026-05-24"), repository.requestedEndDates.last())

        viewModel.selectRange(TrendsRange.MONTH)
        viewModel.selectPreviousPeriod()
        advanceUntilIdle()
        assertEquals(LocalDate.parse("2026-04-01"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2026-04-30"), repository.requestedEndDates.last())

        viewModel.selectRange(TrendsRange.YEAR)
        viewModel.selectNextPeriod()
        advanceUntilIdle()
        assertEquals(LocalDate.parse("2027-01-01"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2027-12-31"), repository.requestedEndDates.last())
    }

    @Test
    fun customDateRangeIsSentToRepositoryAndShiftsByRangeLength() = runTest(dispatcher) {
        val repository = FakeTrendsRepository()
        val viewModel = TrendsViewModel(repository)

        viewModel.selectRange(TrendsRange.CUSTOM)
        viewModel.selectStartDate("2026-05-03")
        viewModel.selectEndDate("2026-05-12")
        advanceUntilIdle()

        assertEquals(TrendsRange.CUSTOM, repository.requestedRanges.last())
        assertEquals(LocalDate.parse("2026-05-03"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2026-05-12"), repository.requestedEndDates.last())

        viewModel.selectNextPeriod()
        advanceUntilIdle()
        assertEquals(LocalDate.parse("2026-05-13"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2026-05-22"), repository.requestedEndDates.last())

        viewModel.selectPreviousPeriod()
        advanceUntilIdle()
        assertEquals(LocalDate.parse("2026-05-03"), repository.requestedStartDates.last())
        assertEquals(LocalDate.parse("2026-05-12"), repository.requestedEndDates.last())
    }
}

private class FakeTrendsRepository : TrendsRepository {
    val requestedRanges = mutableListOf<TrendsRange>()
    val requestedStartDates = mutableListOf<LocalDate>()
    val requestedEndDates = mutableListOf<LocalDate>()

    override fun getTrendsStream(
        range: TrendsRange,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<TrendsSummary> {
        requestedRanges += range
        requestedStartDates += startDate
        requestedEndDates += endDate
        return MutableStateFlow(
            TrendsSummary(
                range = range,
                startDate = startDate,
                endDate = endDate,
            ),
        )
    }
}

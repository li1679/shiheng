package com.example.nutritiontracker.feature.trends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.feature.trends.data.TrendsRange
import com.example.nutritiontracker.feature.trends.data.TrendsRepository
import com.example.nutritiontracker.feature.trends.data.TrendsSummary
import com.example.nutritiontracker.feature.weight.data.WeightLog
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TrendsUiState(
    val selectedRange: TrendsRange = TrendsRange.WEEK,
    val anchorDate: LocalDate = LocalDate.now(),
    val summary: TrendsSummary = TrendsSummary(
        range = TrendsRange.WEEK,
        startDate = LocalDate.now(),
        endDate = LocalDate.now(),
    ),
) {
    val weightLogs: List<WeightLog> = summary.weightLogs
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val trendsRepository: TrendsRepository,
) : ViewModel() {
    private val initialDate = LocalDate.now()
    private val selection = MutableStateFlow(
        TrendsSelection(
            anchorDate = initialDate,
            customStartDate = initialDate.minusDays(6),
            customEndDate = initialDate,
        ),
    )

    val uiState: StateFlow<TrendsUiState> = selection
        .flatMapLatest { currentSelection ->
            val dateRange = currentSelection.dateRange()
            trendsRepository.getTrendsStream(
                range = currentSelection.range,
                startDate = dateRange.start,
                endDate = dateRange.end,
            )
        }
        .map { summary ->
            val currentSelection = selection.value
            TrendsUiState(
                selectedRange = currentSelection.range,
                anchorDate = currentSelection.anchorDate,
                summary = summary.copy(weightLogs = summary.weightLogs.sortedByDescending { it.date }),
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TrendsUiState())

    fun selectRange(range: TrendsRange) {
        selection.value = selection.value.copy(range = range)
    }

    fun selectAnchorDate(date: String) {
        selection.value = selection.value.copy(anchorDate = LocalDate.parse(date))
    }

    fun selectStartDate(date: String) {
        val parsedDate = LocalDate.parse(date)
        val current = selection.value
        selection.value = current.copy(
            range = TrendsRange.CUSTOM,
            anchorDate = parsedDate,
            customStartDate = parsedDate,
            customEndDate = if (parsedDate > current.customEndDate) parsedDate else current.customEndDate,
        )
    }

    fun selectEndDate(date: String) {
        val parsedDate = LocalDate.parse(date)
        val current = selection.value
        selection.value = current.copy(
            range = TrendsRange.CUSTOM,
            anchorDate = parsedDate,
            customStartDate = if (parsedDate < current.customStartDate) parsedDate else current.customStartDate,
            customEndDate = parsedDate,
        )
    }

    fun selectPreviousPeriod() {
        selection.value = selection.value.shift(direction = -1)
    }

    fun selectNextPeriod() {
        selection.value = selection.value.shift(direction = 1)
    }

    fun selectToday() {
        selection.value = selection.value.copy(anchorDate = LocalDate.now(), range = TrendsRange.WEEK)
    }
}

private data class TrendsSelection(
    val range: TrendsRange = TrendsRange.WEEK,
    val anchorDate: LocalDate,
    val customStartDate: LocalDate,
    val customEndDate: LocalDate,
) {
    fun dateRange(): TrendsDateRange =
        when (range) {
            TrendsRange.CUSTOM -> TrendsDateRange(start = customStartDate, end = customEndDate)
            else -> anchorDate.quickDateRange(range)
        }

    fun shift(direction: Long): TrendsSelection =
        if (range == TrendsRange.CUSTOM) {
            val dayCount = ChronoUnit.DAYS.between(customStartDate, customEndDate) + 1
            copy(
                anchorDate = anchorDate.plusDays(dayCount * direction),
                customStartDate = customStartDate.plusDays(dayCount * direction),
                customEndDate = customEndDate.plusDays(dayCount * direction),
            )
        } else {
            copy(anchorDate = anchorDate.shiftByRange(range, direction))
        }
}

private data class TrendsDateRange(val start: LocalDate, val end: LocalDate)

private fun LocalDate.shiftByRange(range: TrendsRange, direction: Long): LocalDate =
    when (range) {
        TrendsRange.WEEK -> plusWeeks(direction)
        TrendsRange.MONTH -> plusMonths(direction)
        TrendsRange.YEAR -> plusYears(direction)
        TrendsRange.CUSTOM -> plusDays(direction)
    }

private fun LocalDate.quickDateRange(range: TrendsRange): TrendsDateRange =
    when (range) {
        TrendsRange.WEEK -> TrendsDateRange(
            start = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            end = with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)),
        )

        TrendsRange.MONTH -> YearMonth.from(this).let { month ->
            TrendsDateRange(start = month.atDay(1), end = month.atEndOfMonth())
        }

        TrendsRange.YEAR -> Year.from(this).let { year ->
            TrendsDateRange(start = year.atDay(1), end = year.atMonth(12).atEndOfMonth())
        }

        TrendsRange.CUSTOM -> TrendsDateRange(start = this, end = this)
    }

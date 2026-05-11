package com.example.nutritiontracker.feature.today.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.feature.today.data.TodayRepository
import com.example.nutritiontracker.feature.today.data.TodaySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TodayUiState(
    val summary: TodaySummary = TodaySummary(date = LocalDate.now().toString()),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: TodayRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now().toString())

    val uiState: StateFlow<TodayUiState> = selectedDate
        .flatMapLatest { date -> repository.getSummaryStream(date) }
        .map { summary -> TodayUiState(summary = summary) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, TodayUiState())

    fun selectDate(date: String) {
        selectedDate.value = date
    }

    fun selectPreviousDate() {
        selectedDate.value = LocalDate.parse(selectedDate.value).minusDays(1).toString()
    }

    fun selectNextDate() {
        selectedDate.value = LocalDate.parse(selectedDate.value).plusDays(1).toString()
    }

    fun selectToday() {
        selectedDate.value = LocalDate.now().toString()
    }
}

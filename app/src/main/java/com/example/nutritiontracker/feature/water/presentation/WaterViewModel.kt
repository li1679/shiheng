package com.example.nutritiontracker.feature.water.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.feature.water.data.WaterLog
import com.example.nutritiontracker.feature.water.data.WaterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WaterUiState(
    val date: String = LocalDate.now().toString(),
    val waterLog: WaterLog = WaterLog(date = date, totalMl = 0),
    val dailyWaterGoalMl: Int = 2000,
    val quickAddMlValues: List<Int> = listOf(250, 500),
    val errorMessage: String? = null,
) {
    val totalMl: Int = waterLog.totalMl
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WaterViewModel @Inject constructor(
    private val repository: WaterRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now().toString())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val waterLog = selectedDate.flatMapLatest { date -> repository.getWaterLogStream(date) }

    val uiState: StateFlow<WaterUiState> = combine(
        selectedDate,
        waterLog,
        userPreferencesRepository.userPreferencesStream,
        errorMessage,
    ) { date, waterLog, preferences, error ->
        WaterUiState(
            date = date,
            waterLog = waterLog,
            dailyWaterGoalMl = preferences.dailyWaterGoalMl,
            quickAddMlValues = preferences.quickWaterMlValues,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, WaterUiState())

    fun selectDate(date: String) {
        selectedDate.value = date
        errorMessage.value = null
    }

    fun quickAdd(amountMl: Int) {
        if (amountMl <= 0) {
            errorMessage.value = "饮水量必须大于 0"
            return
        }

        viewModelScope.launch {
            repository.addWater(selectedDate.value, amountMl)
            errorMessage.value = null
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteWaterEntry(id)
            errorMessage.value = null
        }
    }
}

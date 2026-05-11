package com.example.nutritiontracker.feature.weight.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.feature.weight.data.WeightLog
import com.example.nutritiontracker.feature.weight.data.WeightRepository
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

data class WeightDraft(
    val weightKg: String = "",
    val note: String = "",
)

data class WeightUiState(
    val date: String = LocalDate.now().toString(),
    val weightLog: WeightLog? = null,
    val draft: WeightDraft = WeightDraft(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repository: WeightRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now().toString())
    private val draft = MutableStateFlow(WeightDraft())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)
    private val weightLog = selectedDate.flatMapLatest { date -> repository.getWeightLogStream(date) }

    val uiState: StateFlow<WeightUiState> = combine(
        selectedDate,
        weightLog,
        draft,
        errorMessage,
        successMessage,
    ) { date, weightLog, draft, error, success ->
        WeightUiState(
            date = date,
            weightLog = weightLog,
            draft = draft,
            errorMessage = error,
            successMessage = success,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, WeightUiState())

    fun selectDate(date: String) {
        selectedDate.value = date
        errorMessage.value = null
        successMessage.value = null
    }

    fun onWeightChange(value: String) {
        draft.value = draft.value.copy(weightKg = value)
        errorMessage.value = null
        successMessage.value = null
    }

    fun onNoteChange(value: String) {
        draft.value = draft.value.copy(note = value)
        errorMessage.value = null
        successMessage.value = null
    }

    fun saveWeight(): Boolean {
        val weight = draft.value.weightKg.toDoubleOrNull()
        if (weight == null) {
            errorMessage.value = "体重必须填写数字"
            successMessage.value = null
            return false
        }
        if (weight <= 0.0) {
            errorMessage.value = "体重必须大于 0"
            successMessage.value = null
            return false
        }

        val note = draft.value.note
        viewModelScope.launch {
            repository.saveWeight(selectedDate.value, weight, note)
            errorMessage.value = null
            successMessage.value = "体重已保存"
        }
        return true
    }
}

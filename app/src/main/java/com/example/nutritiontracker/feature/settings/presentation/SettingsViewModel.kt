package com.example.nutritiontracker.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.core.datastore.UserPreferences
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsDraft(
    val defaultFoodBaseWeightGram: String = "500",
    val dailyWaterGoalMl: String = "2000",
    val quickWaterMlValues: String = "250, 500",
    val quickWaterInput: String = "",
    val themeMode: String = "system",
)

data class SettingsUiState(
    val draft: SettingsDraft = SettingsDraft(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val draft = MutableStateFlow(SettingsDraft())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)
    private var hasLocalEdits = false

    val uiState: StateFlow<SettingsUiState> = combine(
        draft,
        errorMessage,
        successMessage,
    ) { draft, error, success ->
        SettingsUiState(draft = draft, errorMessage = error, successMessage = success)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesStream.collect { preferences ->
                if (!hasLocalEdits) {
                    draft.value = preferences.toDraft()
                }
            }
        }
    }

    fun onDefaultFoodBaseWeightChange(value: String) {
        hasLocalEdits = true
        draft.value = draft.value.copy(defaultFoodBaseWeightGram = value)
        persistDraftIfValid()
    }

    fun onDailyWaterGoalChange(value: String) {
        hasLocalEdits = true
        draft.value = draft.value.copy(dailyWaterGoalMl = value)
        persistDraftIfValid()
    }

    fun onQuickWaterValuesChange(value: String) {
        hasLocalEdits = true
        draft.value = draft.value.copy(quickWaterMlValues = value)
        persistDraftIfValid()
    }

    fun onQuickWaterInputChange(value: String) {
        draft.value = draft.value.copy(quickWaterInput = value)
        clearMessages()
    }

    fun addQuickWaterValue(): Boolean {
        val amount = draft.value.quickWaterInput.trim().toIntOrNull()
            ?: return failQuickWaterInput("新增杯子必须填写数字")
        if (amount <= 0) return failQuickWaterInput("新增杯子必须大于 0")

        val values = draft.value.quickWaterValuesOrEmpty() + amount
        hasLocalEdits = true
        draft.value = draft.value.copy(
            quickWaterMlValues = values.joinToString(", "),
            quickWaterInput = "",
        )
        return persistDraftIfValid()
    }

    fun removeQuickWaterValue(amountMl: Int) {
        val values = draft.value.quickWaterValuesOrEmpty().toMutableList()
        values.remove(amountMl)
        hasLocalEdits = true
        draft.value = draft.value.copy(quickWaterMlValues = values.joinToString(", "))
        persistDraftIfValid()
    }

    fun onThemeModeChange(value: String) {
        hasLocalEdits = true
        draft.value = draft.value.copy(themeMode = value)
        persistDraftIfValid()
    }

    fun saveSettings(): Boolean {
        return persistDraftIfValid()
    }

    private fun persistDraftIfValid(): Boolean {
        val result = draft.value.toPreferencesOrError()
        if (result.error != null) {
            errorMessage.value = result.error
            successMessage.value = null
            return false
        }

        viewModelScope.launch {
            userPreferencesRepository.updatePreferences(result.preferences!!)
            hasLocalEdits = false
            errorMessage.value = null
            successMessage.value = null
        }
        return true
    }

    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }

    private fun failQuickWaterInput(message: String): Boolean {
        errorMessage.value = message
        successMessage.value = null
        return false
    }
}

private data class SettingsInputResult(
    val preferences: UserPreferences? = null,
    val error: String? = null,
)

private fun SettingsDraft.toPreferencesOrError(): SettingsInputResult {
    val defaultFoodBaseWeight = defaultFoodBaseWeightGram.toDoubleOrNull()
        ?: return SettingsInputResult(error = "默认基准重量必须填写数字")
    if (defaultFoodBaseWeight <= 0.0) {
        return SettingsInputResult(error = "默认基准重量必须大于 0")
    }

    val waterGoal = dailyWaterGoalMl.toIntOrNull()
        ?: return SettingsInputResult(error = "喝水目标必须填写数字")
    if (waterGoal <= 0) return SettingsInputResult(error = "喝水目标必须大于 0")

    val quickWaterValues = quickWaterMlValues
        .trim()
        .split(Regex("[,，\\s]+"))
        .filter { it.isNotBlank() }
        .map {
            it.toIntOrNull() ?: return SettingsInputResult(error = "快捷饮水值必须填写数字")
        }
    if (quickWaterValues.isEmpty()) return SettingsInputResult(error = "快捷饮水值不能为空")
    if (quickWaterValues.any { it <= 0 }) return SettingsInputResult(error = "快捷饮水值必须大于 0")

    val mode = themeMode.trim().ifEmpty { "system" }
    if (mode !in setOf("system", "light", "dark")) {
        return SettingsInputResult(error = "主题只能是 system、light 或 dark")
    }

    return SettingsInputResult(
        preferences = UserPreferences(
            defaultFoodBaseWeightGram = defaultFoodBaseWeight,
            dailyWaterGoalMl = waterGoal,
            quickWaterMlValues = quickWaterValues,
            themeMode = mode,
        ),
    )
}

private fun SettingsDraft.quickWaterValuesOrEmpty(): List<Int> =
    quickWaterMlValues
        .trim()
        .split(Regex("[,，\\s]+"))
        .filter { it.isNotBlank() }
        .mapNotNull { it.toIntOrNull() }

private fun UserPreferences.toDraft(): SettingsDraft = SettingsDraft(
    defaultFoodBaseWeightGram = defaultFoodBaseWeightGram.formatInput(),
    dailyWaterGoalMl = dailyWaterGoalMl.toString(),
    quickWaterMlValues = quickWaterMlValues.joinToString(", "),
    themeMode = themeMode,
)

private fun Double.formatInput(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)

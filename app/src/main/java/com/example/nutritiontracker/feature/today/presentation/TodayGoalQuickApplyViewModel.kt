package com.example.nutritiontracker.feature.today.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.feature.goals.data.GoalRepository
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayGoalQuickApplyUiState(
    val templates: List<GoalTemplate> = emptyList(),
    val selectedTemplateId: Long? = null,
    val selectedTemplate: GoalTemplate? = null,
    val manualDraft: ManualGoalDraft = ManualGoalDraft(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

data class ManualGoalDraft(
    val proteinGoalGram: String = "",
    val fatGoalGram: String = "",
    val carbGoalGram: String = "",
)

@HiltViewModel
class TodayGoalQuickApplyViewModel @Inject constructor(
    private val repository: GoalRepository,
) : ViewModel() {
    private val selectedTemplateId = MutableStateFlow<Long?>(null)
    private val manualDraft = MutableStateFlow(ManualGoalDraft())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TodayGoalQuickApplyUiState> = combine(
        repository.getTemplatesStream(),
        selectedTemplateId,
        manualDraft,
        errorMessage,
        successMessage,
    ) { templates, selectedId, manualDraft, error, success ->
        val resolvedId = selectedId ?: templates.firstOrNull()?.id
        TodayGoalQuickApplyUiState(
            templates = templates,
            selectedTemplateId = resolvedId,
            selectedTemplate = templates.firstOrNull { it.id == resolvedId },
            manualDraft = manualDraft,
            errorMessage = error,
            successMessage = success,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TodayGoalQuickApplyUiState())

    fun selectTemplate(id: Long) {
        selectedTemplateId.value = id
        clearMessages()
    }

    fun onManualProteinGoalChange(value: String) {
        manualDraft.value = manualDraft.value.copy(proteinGoalGram = value)
        clearMessages()
    }

    fun onManualFatGoalChange(value: String) {
        manualDraft.value = manualDraft.value.copy(fatGoalGram = value)
        clearMessages()
    }

    fun onManualCarbGoalChange(value: String) {
        manualDraft.value = manualDraft.value.copy(carbGoalGram = value)
        clearMessages()
    }

    fun saveManualGoal(date: String): Boolean {
        val startDate = date.toLocalDateOrNull() ?: return fail("日期格式必须是 yyyy-MM-dd")
        val input = manualDraft.value.toInputOrError()
        if (input.error != null) return fail(input.error)

        viewModelScope.launch {
            repository.saveDailyGoal(
                date = startDate,
                proteinGoalGram = input.proteinGoalGram,
                fatGoalGram = input.fatGoalGram,
                carbGoalGram = input.carbGoalGram,
            )
            errorMessage.value = null
            successMessage.value = "已保存这一天目标"
        }
        return true
    }

    fun saveMacroGoal(
        date: String,
        type: MacroGoalType,
        value: String,
        currentProteinGoal: Double?,
        currentFatGoal: Double?,
        currentCarbGoal: Double?,
    ): Boolean {
        val startDate = date.toLocalDateOrNull() ?: return fail("日期格式必须是 yyyy-MM-dd")
        val goal = value.toDoubleOrNull() ?: return fail("${type.label}目标必须填写数字")
        if (goal < 0.0) return fail("${type.label}目标不能为负数")

        val protein = if (type == MacroGoalType.Protein) goal else currentProteinGoal ?: 0.0
        val fat = if (type == MacroGoalType.Fat) goal else currentFatGoal ?: 0.0
        val carb = if (type == MacroGoalType.Carb) goal else currentCarbGoal ?: 0.0
        viewModelScope.launch {
            repository.saveDailyGoal(
                date = startDate,
                proteinGoalGram = protein,
                fatGoalGram = fat,
                carbGoalGram = carb,
            )
            errorMessage.value = null
            successMessage.value = "已保存${type.label}目标"
        }
        return true
    }

    fun clearMacroGoal(
        date: String,
        type: MacroGoalType,
        currentProteinGoal: Double?,
        currentFatGoal: Double?,
        currentCarbGoal: Double?,
    ): Boolean {
        val startDate = date.toLocalDateOrNull() ?: return fail("日期格式必须是 yyyy-MM-dd")
        val protein = if (type == MacroGoalType.Protein) 0.0 else currentProteinGoal ?: 0.0
        val fat = if (type == MacroGoalType.Fat) 0.0 else currentFatGoal ?: 0.0
        val carb = if (type == MacroGoalType.Carb) 0.0 else currentCarbGoal ?: 0.0
        viewModelScope.launch {
            repository.saveDailyGoal(
                date = startDate,
                proteinGoalGram = protein,
                fatGoalGram = fat,
                carbGoalGram = carb,
            )
            errorMessage.value = null
            successMessage.value = "已删除${type.label}目标"
        }
        return true
    }

    fun applyToDate(date: String): Boolean = apply(date = date, dayCount = 1)

    fun applyForNextThirtyDays(date: String): Boolean = apply(date = date, dayCount = 30)

    private fun apply(date: String, dayCount: Int): Boolean {
        val templateId = uiState.value.selectedTemplateId ?: return fail("先创建目标模板")
        val startDate = date.toLocalDateOrNull() ?: return fail("日期格式必须是 yyyy-MM-dd")

        viewModelScope.launch {
            repository.applyTemplate(
                templateId = templateId,
                startDate = startDate,
                dayCount = dayCount,
                skippedDates = emptySet(),
            )
            errorMessage.value = null
            successMessage.value = if (dayCount == 1) "已应用到 $date" else "已从 $date 应用 30 天"
        }
        return true
    }

    private fun fail(message: String): Boolean {
        errorMessage.value = message
        successMessage.value = null
        return false
    }

    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }
}

private data class ManualGoalInput(
    val proteinGoalGram: Double = 0.0,
    val fatGoalGram: Double = 0.0,
    val carbGoalGram: Double = 0.0,
    val error: String? = null,
)

private fun ManualGoalDraft.toInputOrError(): ManualGoalInput {
    val protein = proteinGoalGram.toDoubleOrNull() ?: return ManualGoalInput(error = "蛋白目标必须填写数字")
    val fat = fatGoalGram.toDoubleOrNull() ?: return ManualGoalInput(error = "脂肪目标必须填写数字")
    val carb = carbGoalGram.toDoubleOrNull() ?: return ManualGoalInput(error = "碳水目标必须填写数字")
    if (protein < 0.0 || fat < 0.0 || carb < 0.0) return ManualGoalInput(error = "三大营养素目标不能为负数")
    return ManualGoalInput(proteinGoalGram = protein, fatGoalGram = fat, carbGoalGram = carb)
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(trim())
} catch (_: DateTimeParseException) {
    null
}

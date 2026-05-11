package com.example.nutritiontracker.feature.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.feature.goals.data.GoalRepository
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.goals.data.GoalTemplateInput
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

data class GoalTemplateDraft(
    val name: String = "",
    val proteinGoalGram: String = "",
    val fatGoalGram: String = "",
    val carbGoalGram: String = "",
)

data class ApplyGoalDraft(
    val selectedTemplateId: Long? = null,
    val startDate: String = LocalDate.now().toString(),
    val dayCount: String = "30",
    val skippedDates: String = "",
)

data class GoalPreviewDay(
    val date: LocalDate,
    val isSkipped: Boolean,
)

data class GoalsUiState(
    val templates: List<GoalTemplate> = emptyList(),
    val templateDraft: GoalTemplateDraft = GoalTemplateDraft(),
    val applyDraft: ApplyGoalDraft = ApplyGoalDraft(),
    val previewDates: List<GoalPreviewDay> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalRepository,
) : ViewModel() {
    private val templateDraft = MutableStateFlow(GoalTemplateDraft())
    private val applyDraft = MutableStateFlow(ApplyGoalDraft())
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GoalsUiState> = combine(
        repository.getTemplatesStream(),
        templateDraft,
        applyDraft,
        errorMessage,
        successMessage,
    ) { templates, templateDraft, applyDraft, error, success ->
        val resolvedApplyDraft = applyDraft.ensureSelectedTemplate(templates)
        GoalsUiState(
            templates = templates,
            templateDraft = templateDraft,
            applyDraft = resolvedApplyDraft,
            previewDates = resolvedApplyDraft.previewDates(),
            errorMessage = error,
            successMessage = success,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GoalsUiState())

    fun onTemplateNameChange(value: String) {
        templateDraft.value = templateDraft.value.copy(name = value)
        clearMessages()
    }

    fun onProteinChange(value: String) {
        templateDraft.value = templateDraft.value.copy(proteinGoalGram = value)
        clearMessages()
    }

    fun onFatChange(value: String) {
        templateDraft.value = templateDraft.value.copy(fatGoalGram = value)
        clearMessages()
    }

    fun onCarbChange(value: String) {
        templateDraft.value = templateDraft.value.copy(carbGoalGram = value)
        clearMessages()
    }

    fun onTemplateSelected(id: Long) {
        applyDraft.value = applyDraft.value.copy(selectedTemplateId = id)
        clearMessages()
    }

    fun onApplyStartDateChange(value: String) {
        applyDraft.value = applyDraft.value.copy(startDate = value)
        clearMessages()
    }

    fun onApplyDayCountChange(value: String) {
        applyDraft.value = applyDraft.value.copy(dayCount = value)
        clearMessages()
    }

    fun onSkippedDatesChange(value: String) {
        applyDraft.value = applyDraft.value.copy(skippedDates = value)
        clearMessages()
    }

    fun toggleSkippedDate(date: LocalDate) {
        val dates = (applyDraft.value.skippedDates.toSkippedDatesOrNull() ?: emptySet()).toMutableSet()
        if (!dates.add(date)) {
            dates.remove(date)
        }
        applyDraft.value = applyDraft.value.copy(skippedDates = dates.sorted().joinToString(", ") { it.toString() })
        clearMessages()
    }

    fun saveTemplate(): Boolean {
        val input = templateDraft.value.toInputOrError()
        if (input.error != null) {
            errorMessage.value = input.error
            successMessage.value = null
            return false
        }

        viewModelScope.launch {
            repository.saveTemplate(input.goalTemplateInput!!)
            templateDraft.value = GoalTemplateDraft()
            errorMessage.value = null
            successMessage.value = "目标模板已保存"
        }
        return true
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
        }
    }

    fun applyTemplate(): Boolean {
        val input = applyDraft.value.toApplyInputOrError(uiState.value.templates.firstOrNull()?.id)
        if (input.error != null) {
            errorMessage.value = input.error
            successMessage.value = null
            return false
        }

        viewModelScope.launch {
            repository.applyTemplate(
                templateId = input.templateId!!,
                startDate = input.startDate!!,
                dayCount = input.dayCount!!,
                skippedDates = input.skippedDates,
            )
            errorMessage.value = null
            successMessage.value = "目标已应用"
        }
        return true
    }

    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }
}

private fun GoalTemplateDraft.toInputOrError(): GoalTemplateInputResult {
    val trimmedName = name.trim()
    if (trimmedName.isEmpty()) return GoalTemplateInputResult(error = "模板名称不能为空")

    val protein = proteinGoalGram.toDoubleOrNull() ?: return GoalTemplateInputResult(error = "蛋白质目标必须填写数字")
    val fat = fatGoalGram.toDoubleOrNull() ?: return GoalTemplateInputResult(error = "脂肪目标必须填写数字")
    val carb = carbGoalGram.toDoubleOrNull() ?: return GoalTemplateInputResult(error = "碳水目标必须填写数字")
    if (protein < 0.0 || fat < 0.0 || carb < 0.0) return GoalTemplateInputResult(error = "三大营养素目标不能为负数")

    return GoalTemplateInputResult(
        goalTemplateInput = GoalTemplateInput(
            name = trimmedName,
            proteinGoalGram = protein,
            fatGoalGram = fat,
            carbGoalGram = carb,
        ),
    )
}

private fun ApplyGoalDraft.toApplyInputOrError(defaultTemplateId: Long?): ApplyGoalInputResult {
    val templateId = selectedTemplateId ?: defaultTemplateId ?: return ApplyGoalInputResult(error = "请选择目标模板")
    val startDate = startDate.toLocalDateOrNull() ?: return ApplyGoalInputResult(error = "开始日期格式必须是 yyyy-MM-dd")
    val dayCount = dayCount.toIntOrNull() ?: return ApplyGoalInputResult(error = "应用天数必须填写整数")
    if (dayCount <= 0) return ApplyGoalInputResult(error = "应用天数必须大于 0")
    val skippedDates = skippedDates.toSkippedDatesOrNull() ?: return ApplyGoalInputResult(error = "跳过日期格式必须是 yyyy-MM-dd")

    return ApplyGoalInputResult(
        templateId = templateId,
        startDate = startDate,
        dayCount = dayCount,
        skippedDates = skippedDates,
    )
}

private fun ApplyGoalDraft.ensureSelectedTemplate(templates: List<GoalTemplate>): ApplyGoalDraft =
    if (selectedTemplateId == null && templates.isNotEmpty()) copy(selectedTemplateId = templates.first().id) else this

private fun ApplyGoalDraft.previewDates(): List<GoalPreviewDay> {
    val startDate = startDate.toLocalDateOrNull() ?: return emptyList()
    val dayCount = dayCount.toIntOrNull() ?: return emptyList()
    if (dayCount <= 0) return emptyList()
    val skipped = skippedDates.toSkippedDatesOrNull() ?: emptySet()
    return (0 until dayCount).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        GoalPreviewDay(date = date, isSkipped = date in skipped)
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(trim())
} catch (_: DateTimeParseException) {
    null
}

private fun String.toSkippedDatesOrNull(): Set<LocalDate>? {
    if (isBlank()) return emptySet()
    return split(',', '，', '\n', ' ')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.toLocalDateOrNull() ?: return null }
        .toSet()
}

private data class GoalTemplateInputResult(
    val goalTemplateInput: GoalTemplateInput? = null,
    val error: String? = null,
)

private data class ApplyGoalInputResult(
    val templateId: Long? = null,
    val startDate: LocalDate? = null,
    val dayCount: Int? = null,
    val skippedDates: Set<LocalDate> = emptySet(),
    val error: String? = null,
)

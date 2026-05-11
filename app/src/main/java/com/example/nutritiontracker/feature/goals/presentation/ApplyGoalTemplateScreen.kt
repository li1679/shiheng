package com.example.nutritiontracker.feature.goals.presentation

import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.core.ui.MacroChip
import com.example.nutritiontracker.core.ui.NutritionCard
import com.example.nutritiontracker.core.ui.PageHeader
import com.example.nutritiontracker.core.ui.SectionTitle
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein

@Composable
fun ApplyGoalTemplateScreen(
    uiState: GoalsUiState,
    onTemplateSelected: (Long) -> Unit,
    onStartDateChange: (String) -> Unit,
    onDayCountChange: (String) -> Unit,
    onSkippedDatesChange: (String) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(title = "应用目标模板", subtitle = "")

        CollapsibleGoalTemplateSelector(
            templates = uiState.templates,
            selectedTemplateId = uiState.applyDraft.selectedTemplateId,
            onTemplateSelected = onTemplateSelected,
        )

        NutritionCard(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            OutlinedTextField(
                value = uiState.applyDraft.startDate,
                onValueChange = onStartDateChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("开始日期") },
                supportingText = { Text("格式：2026-05-11") },
            )
            OutlinedTextField(
                value = uiState.applyDraft.dayCount,
                onValueChange = onDayCountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("应用天数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = uiState.applyDraft.skippedDates,
                onValueChange = onSkippedDatesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("跳过日期") },
                supportingText = { Text("多个日期用逗号或换行分开") },
            )

            SectionTitle("日期预览")
            if (uiState.previewDates.isEmpty()) {
                Text("先填写开始日期和应用天数", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                uiState.previewDates.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        week.forEach { day ->
                            PreviewDateChip(
                                date = day.date,
                                skipped = day.isSkipped,
                                onClick = { onSkippedDatesChange(toggleDateText(uiState.applyDraft.skippedDates, day.date.toString())) },
                            )
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.successMessage != null) {
                Text(uiState.successMessage, color = MaterialTheme.colorScheme.primary)
            }

            Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text("应用到日历")
            }
        }
    }
}

@Composable
private fun CollapsibleGoalTemplateSelector(
    templates: List<GoalTemplate>,
    selectedTemplateId: Long?,
    onTemplateSelected: (Long) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    SectionTitle("选择模板")
    if (templates.isEmpty()) {
        Text("还没有可应用的模板", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val selectedTemplate = templates.firstOrNull { it.id == selectedTemplateId } ?: templates.first()
    GoalTemplateChoice(
        template = selectedTemplate,
        selected = true,
        onClick = { expanded = !expanded },
        contentDescription = null,
    )
    TextButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.semantics {
            contentDescription = if (expanded) "收起目标模板" else "展开目标模板"
        },
    ) {
        Text(if (expanded) "收起模板列表" else "更换模板")
    }
    AnimatedVisibility(visible = expanded) {
        templates
            .filterNot { it.id == selectedTemplate.id }
            .forEach { template ->
                GoalTemplateChoice(
                    template = template,
                    selected = false,
                    onClick = {
                        onTemplateSelected(template.id)
                        expanded = false
                    },
                    contentDescription = null,
                )
            }
    }
}

@Composable
private fun PreviewDateChip(
    date: java.time.LocalDate,
    skipped: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (skipped) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (skipped) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun GoalTemplateChoice(
    template: GoalTemplate,
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String?,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(
            if (contentDescription == null) {
                Modifier
            } else {
                Modifier.semantics { this.contentDescription = contentDescription }
            },
        )
        .clickable(onClick = onClick)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (selected) Text("已选", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroChip("蛋白", template.proteinGoalGram, MacroProtein)
                MacroChip("脂肪", template.fatGoalGram, MacroFat)
                MacroChip("碳水", template.carbGoalGram, MacroCarb)
            }
            Text(
                "蛋白 ${template.proteinGoalGram.formatNutritionNumber()}g，脂肪 ${template.fatGoalGram.formatNutritionNumber()}g，碳水 ${template.carbGoalGram.formatNutritionNumber()}g",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun toggleDateText(currentText: String, date: String): String {
    val dates = currentText
        .split(',', '，', '\n', ' ')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toMutableSet()
    if (!dates.add(date)) {
        dates.remove(date)
    }
    return dates.sorted().joinToString(", ")
}

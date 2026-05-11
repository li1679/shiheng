package com.example.nutritiontracker.feature.goals.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.core.ui.MacroChip
import com.example.nutritiontracker.core.ui.PageHeader
import com.example.nutritiontracker.core.ui.NutritionAnimatedDialog
import com.example.nutritiontracker.core.ui.SectionTitle
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein

@Composable
fun GoalsScreen(
    uiState: GoalsUiState,
    onTemplateNameChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onCarbChange: (String) -> Unit,
    onSaveTemplate: () -> Boolean,
    onDeleteTemplate: (Long) -> Unit,
    onOpenApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var creatingTemplate by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(
            title = "目标",
            subtitle = "",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { creatingTemplate = true }) {
                    Text("新建模板")
                }
                FilledTonalButton(onClick = onOpenApply) {
                    Text("应用模板")
                }
            }
        }

        SectionTitle("目标模板")
        if (uiState.templates.isEmpty()) {
            Text("还没有模板", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(uiState.templates, key = { it.id }) { template ->
                    GoalTemplateRow(template = template, onDelete = { onDeleteTemplate(template.id) })
                }
            }
        }
    }

    if (creatingTemplate) {
        CreateGoalTemplateDialog(
            uiState = uiState,
            onTemplateNameChange = onTemplateNameChange,
            onProteinChange = onProteinChange,
            onFatChange = onFatChange,
            onCarbChange = onCarbChange,
            onSaveTemplate = {
                if (onSaveTemplate()) {
                    creatingTemplate = false
                }
            },
            onDismiss = { creatingTemplate = false },
        )
    }
}

@Composable
private fun CreateGoalTemplateDialog(
    uiState: GoalsUiState,
    onTemplateNameChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onCarbChange: (String) -> Unit,
    onSaveTemplate: () -> Unit,
    onDismiss: () -> Unit,
) {
    NutritionAnimatedDialog(onDismissRequest = onDismiss) {
        Text("新建模板", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = uiState.templateDraft.name,
            onValueChange = onTemplateNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("模板名称") },
        )
        NumberField(value = uiState.templateDraft.proteinGoalGram, onValueChange = onProteinChange, label = "蛋白目标（g）")
        NumberField(value = uiState.templateDraft.fatGoalGram, onValueChange = onFatChange, label = "脂肪目标（g）")
        NumberField(value = uiState.templateDraft.carbGoalGram, onValueChange = onCarbChange, label = "碳水目标（g）")
        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
        if (uiState.successMessage != null) {
            Text(uiState.successMessage, color = MaterialTheme.colorScheme.primary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            TextButton(onClick = onSaveTemplate) {
                Text("保存")
            }
        }
    }
}

@Composable
private fun GoalTemplateRow(template: GoalTemplate, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onDelete) {
                    Text("删除")
                }
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

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

package com.example.nutritiontracker.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.core.ui.NutritionCard
import com.example.nutritiontracker.core.ui.NutritionAnimatedDialog
import com.example.nutritiontracker.core.ui.PageHeader

@Composable
fun SettingsFeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onDefaultFoodBaseWeightChange = viewModel::onDefaultFoodBaseWeightChange,
        onDailyWaterGoalChange = viewModel::onDailyWaterGoalChange,
        onQuickWaterValuesChange = viewModel::onQuickWaterValuesChange,
        onQuickWaterInputChange = viewModel::onQuickWaterInputChange,
        onAddQuickWaterValue = { viewModel.addQuickWaterValue() },
        onRemoveQuickWaterValue = viewModel::removeQuickWaterValue,
        onThemeModeChange = viewModel::onThemeModeChange,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onDefaultFoodBaseWeightChange: (String) -> Unit,
    onDailyWaterGoalChange: (String) -> Unit,
    onQuickWaterValuesChange: (String) -> Unit,
    onQuickWaterInputChange: (String) -> Unit = {},
    onAddQuickWaterValue: () -> Unit = {},
    onRemoveQuickWaterValue: (Int) -> Unit = {},
    onThemeModeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteQuickWaterAmount by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(title = "设置", subtitle = "")

        NutritionCard(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            NumberField(
                value = uiState.draft.defaultFoodBaseWeightGram,
                onValueChange = onDefaultFoodBaseWeightChange,
                label = "默认食物基准重量（g）",
                keyboardType = KeyboardType.Decimal,
            )
            NumberField(
                value = uiState.draft.dailyWaterGoalMl,
                onValueChange = onDailyWaterGoalChange,
                label = "每日喝水目标（ml）",
                keyboardType = KeyboardType.Number,
            )
            QuickWaterEditor(
                quickValuesText = uiState.draft.quickWaterMlValues,
                quickInput = uiState.draft.quickWaterInput,
                onQuickWaterValuesChange = onQuickWaterValuesChange,
                onQuickWaterInputChange = onQuickWaterInputChange,
                onAddQuickWaterValue = onAddQuickWaterValue,
                onRemoveQuickWaterValue = { amount -> pendingDeleteQuickWaterAmount = amount },
            )
            ThemeModeSelector(
                selected = uiState.draft.themeMode,
                onThemeModeChange = onThemeModeChange,
            )
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    val deletingQuickWaterAmount = pendingDeleteQuickWaterAmount
    if (deletingQuickWaterAmount != null) {
        NutritionAnimatedDialog(
            onDismissRequest = { pendingDeleteQuickWaterAmount = null },
        ) {
            Text("删除快捷杯子", style = MaterialTheme.typography.headlineSmall)
            Text(
                "确定删除 ${deletingQuickWaterAmount}ml 吗？",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { pendingDeleteQuickWaterAmount = null }) {
                    Text("取消")
                }
                TextButton(
                    onClick = {
                        onRemoveQuickWaterValue(deletingQuickWaterAmount)
                        pendingDeleteQuickWaterAmount = null
                    },
                ) {
                    Text("删除")
                }
            }
        }
    }
}

@Composable
private fun QuickWaterEditor(
    quickValuesText: String,
    quickInput: String,
    onQuickWaterValuesChange: (String) -> Unit,
    onQuickWaterInputChange: (String) -> Unit,
    onAddQuickWaterValue: () -> Unit,
    onRemoveQuickWaterValue: (Int) -> Unit,
) {
    val quickValues = quickValuesText.parseQuickWaterValues()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("快捷杯子", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quickValues.forEach { amount ->
                QuickWaterChip(amount = amount, onRemoveQuickWaterValue = onRemoveQuickWaterValue)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = quickInput,
                onValueChange = onQuickWaterInputChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("新增杯子（ml）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            FilledTonalButton(onClick = onAddQuickWaterValue, modifier = Modifier.padding(top = 8.dp)) {
                Text("添加杯子")
            }
        }
    }
}

@Composable
private fun QuickWaterChip(
    amount: Int,
    onRemoveQuickWaterValue: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.clickable { onRemoveQuickWaterValue(amount) },
    ) {
        Text(
            text = "${amount}ml",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
private fun ThemeModeSelector(
    selected: String,
    onThemeModeChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("主题", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip("跟随系统", "system", selected, onThemeModeChange)
            ThemeChip("浅色", "light", selected, onThemeModeChange)
            ThemeChip("深色", "dark", selected, onThemeModeChange)
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    value: String,
    selected: String,
    onThemeModeChange: (String) -> Unit,
) {
    FilterChip(
        selected = selected == value,
        onClick = { onThemeModeChange(value) },
        label = { Text(label) },
    )
}

private fun String.parseQuickWaterValues(): List<Int> =
    trim()
        .split(Regex("[,，\\s]+"))
        .filter { it.isNotBlank() }
        .mapNotNull { it.toIntOrNull() }

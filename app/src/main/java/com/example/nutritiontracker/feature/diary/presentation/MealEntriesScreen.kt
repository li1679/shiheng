package com.example.nutritiontracker.feature.diary.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.core.ui.EmptyState
import com.example.nutritiontracker.core.ui.MacroChip
import com.example.nutritiontracker.core.ui.NutritionCard
import com.example.nutritiontracker.core.ui.PageHeader
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.diary.data.FoodEntry
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein

@Composable
fun MealEntriesScreen(
    uiState: DiaryUiState,
    onAddEntry: () -> Unit,
    onCopyYesterdayMeal: () -> Unit = {},
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(title = uiState.mealType.label, subtitle = uiState.date)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onAddEntry, modifier = Modifier.weight(1f)) {
                Text("添加食物")
            }
            Button(onClick = onCopyYesterdayMeal, modifier = Modifier.weight(1f)) {
                Text("复制昨天本餐")
            }
        }

        if (uiState.successMessage != null) {
            Text(uiState.successMessage, color = MaterialTheme.colorScheme.primary)
        }

        if (uiState.entries.isEmpty()) {
            EmptyState(
                title = "这个餐次还没有记录",
                body = "",
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    MealSummaryCard(entries = uiState.entries)
                }
                items(uiState.entries, key = { it.id }) { entry ->
                    FoodEntryRow(entry = entry, onDelete = { onDeleteEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun MealSummaryCard(entries: List<FoodEntry>) {
    val protein = entries.sumOf { it.macros.proteinGram }
    val fat = entries.sumOf { it.macros.fatGram }
    val carb = entries.sumOf { it.macros.carbGram }
    val calories = entries.sumOf { it.caloriesKcal }

    NutritionCard(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("餐次汇总", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("共 ${entries.size} 条记录", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${calories.formatNutritionNumber()} kcal", style = MaterialTheme.typography.titleLarge)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroChip("蛋白", protein, MacroProtein)
            MacroChip("脂肪", fat, MacroFat)
            MacroChip("碳水", carb, MacroCarb)
        }
    }
}

@Composable
private fun FoodEntryRow(entry: FoodEntry, onDelete: () -> Unit) {
    NutritionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(entry.foodName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${entry.actualWeightGram.formatNutritionNumber()}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onDelete) {
                Text("删除")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroChip("蛋白", entry.macros.proteinGram, MacroProtein)
            MacroChip("脂肪", entry.macros.fatGram, MacroFat)
            MacroChip("碳水", entry.macros.carbGram, MacroCarb)
        }
        Text("${entry.caloriesKcal.formatNutritionNumber()} kcal", style = MaterialTheme.typography.labelLarge)
    }
}

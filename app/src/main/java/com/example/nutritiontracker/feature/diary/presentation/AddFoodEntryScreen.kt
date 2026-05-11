package com.example.nutritiontracker.feature.diary.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.core.ui.EmptyState
import com.example.nutritiontracker.core.ui.MacroChip
import com.example.nutritiontracker.core.ui.PageHeader
import com.example.nutritiontracker.core.ui.SectionTitle
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein

@Composable
fun AddFoodEntryScreen(
    uiState: DiaryUiState,
    onFoodSelected: (Long) -> Unit,
    onActualWeightChange: (String) -> Unit,
    onFoodSearchQueryChange: (String) -> Unit = {},
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredFoods = uiState.foods
        .filter { it.name.contains(uiState.foodSearchQuery, ignoreCase = true) }
        .sortedWith(
            compareByDescending<Food> { it.id == uiState.selectedFoodId }
                .thenByDescending { it.isFavorite }
                .thenByDescending { it.lastLoggedAt ?: 0L }
                .thenByDescending { it.logCount }
                .thenBy { it.name.lowercase() },
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(title = "添加${uiState.mealType.label}", subtitle = "")

        OutlinedTextField(
            value = uiState.actualWeightGram,
            onValueChange = onActualWeightChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("实际吃了多少（g）") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }

        SectionTitle("食物库")
        OutlinedTextField(
            value = uiState.foodSearchQuery,
            onValueChange = onFoodSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("搜索食物") },
        )
        if (uiState.foods.isEmpty()) {
            EmptyState(
                title = "还没有食物",
                body = "",
            )
        } else if (filteredFoods.isEmpty()) {
            EmptyState(
                title = "没有匹配的食物",
                body = "",
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                items(filteredFoods, key = { it.id }) { food ->
                    FoodOptionRow(
                        food = food,
                        selected = uiState.selectedFoodId == food.id,
                        onClick = { onFoodSelected(food.id) },
                    )
                }
            }
        }

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("保存记录")
        }
    }
}

@Composable
private fun FoodOptionRow(food: Food, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (selected) Text("已选", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroChip("蛋白", food.proteinGram, MacroProtein)
                MacroChip("脂肪", food.fatGram, MacroFat)
                MacroChip("碳水", food.carbGram, MacroCarb)
            }
            Text(
                "每 ${food.baseWeightGram.formatNutritionNumber()}g：蛋白 ${food.proteinGram.formatNutritionNumber()}g，脂肪 ${food.fatGram.formatNutritionNumber()}g，碳水 ${food.carbGram.formatNutritionNumber()}g",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

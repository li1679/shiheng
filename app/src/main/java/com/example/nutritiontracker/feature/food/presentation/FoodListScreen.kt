package com.example.nutritiontracker.feature.food.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.nutritiontracker.core.ui.EmptyState
import com.example.nutritiontracker.core.ui.MacroChip
import com.example.nutritiontracker.core.ui.PageHeader
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein
import java.io.File

@Composable
fun FoodListScreen(
    foods: List<Food>,
    onAddFood: () -> Unit,
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onToggleFavorite: (Long, Boolean) -> Unit = { _, _ -> },
) {
    val filteredFoods = foods.filter { it.name.contains(query, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        PageHeader(
            title = "食物库",
            subtitle = "",
        ) {
            FilledTonalButton(onClick = onAddFood) {
                Text("新建")
            }
        }

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("搜索食物") },
        )

        Spacer(Modifier.height(18.dp))

        if (foods.isEmpty()) {
            EmptyState(
                title = "先创建第一个食物",
                body = "",
                actionLabel = "创建食物",
                onAction = onAddFood,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(filteredFoods, key = { it.id }) { food ->
                    FoodRow(food = food, onToggleFavorite = onToggleFavorite)
                }
            }
        }
    }
}

@Composable
private fun FoodRow(food: Food, onToggleFavorite: (Long, Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FoodThumbnail(food = food)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "每 ${food.baseWeightGram.formatNutritionNumber()}g",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        IconButton(onClick = { onToggleFavorite(food.id, !food.isFavorite) }) {
                            Icon(
                                imageVector = if (food.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = if (food.isFavorite) "取消收藏 ${food.name}" else "收藏 ${food.name}",
                                tint = if (food.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroChip("蛋白", food.proteinGram, MacroProtein)
                    MacroChip("脂肪", food.fatGram, MacroFat)
                    MacroChip("碳水", food.carbGram, MacroCarb)
                }
            }
        }
    }
}

@Composable
private fun FoodThumbnail(food: Food) {
    if (food.imagePath != null) {
        AsyncImage(
            model = File(food.imagePath),
            contentDescription = "食物图片",
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                food.name.firstOrNull()?.toString() ?: "食",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

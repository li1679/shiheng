package com.example.nutritiontracker

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.nutritiontracker.core.ui.NutritionAnimatedContent
import com.example.nutritiontracker.feature.food.presentation.FoodFeatureScreen
import com.example.nutritiontracker.feature.goals.presentation.GoalsFeatureScreen
import com.example.nutritiontracker.feature.settings.presentation.SettingsFeatureScreen
import com.example.nutritiontracker.feature.today.presentation.TodayFeatureScreen
import com.example.nutritiontracker.feature.trends.presentation.TrendsFeatureScreen

@Composable
fun MainNavigation() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Today) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        modifier = Modifier.safeDrawingPadding(),
    ) { innerPadding ->
        NutritionAnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.padding(innerPadding),
            label = "main-tab",
            isForward = { from, to -> to.ordinal >= from.ordinal },
        ) { tab ->
            when (tab) {
                AppTab.Today -> TodayFeatureScreen()
                AppTab.Food -> FoodFeatureScreen()
                AppTab.Goals -> GoalsFeatureScreen()
                AppTab.Trends -> TrendsFeatureScreen()
                AppTab.Settings -> SettingsFeatureScreen()
            }
        }
    }
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    Today("日记", Icons.Rounded.Today),
    Food("食物", Icons.Rounded.Restaurant),
    Goals("目标", Icons.Rounded.Flag),
    Trends("趋势", Icons.AutoMirrored.Rounded.ShowChart),
    Settings("设置", Icons.Rounded.Settings),
}

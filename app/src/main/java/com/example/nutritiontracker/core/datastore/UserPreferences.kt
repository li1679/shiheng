package com.example.nutritiontracker.core.datastore

data class UserPreferences(
    val defaultFoodBaseWeightGram: Double = 500.0,
    val dailyWaterGoalMl: Int = 2000,
    val quickWaterMlValues: List<Int> = listOf(250, 500),
    val themeMode: String = "system",
)

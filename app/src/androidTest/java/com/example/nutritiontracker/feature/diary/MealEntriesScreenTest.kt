package com.example.nutritiontracker.feature.diary

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.nutrition.MacroValues
import com.example.nutritiontracker.feature.diary.data.FoodEntry
import com.example.nutritiontracker.feature.diary.presentation.DiaryUiState
import com.example.nutritiontracker.feature.diary.presentation.MealEntriesScreen
import org.junit.Rule
import org.junit.Test

class MealEntriesScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mealEntriesScreenShowsMealSummaryAndEntries() {
        composeTestRule.setContent {
            MealEntriesScreen(
                uiState = DiaryUiState(
                    date = "2026-05-11",
                    mealType = MealType.LUNCH,
                    entries = listOf(
                        FoodEntry(
                            id = 1L,
                            date = "2026-05-11",
                            mealType = MealType.LUNCH,
                            foodId = 1L,
                            foodName = "米饭",
                            foodImagePath = null,
                            actualWeightGram = 250.0,
                            macros = MacroValues(proteinGram = 5.0, fatGram = 2.5, carbGram = 50.0),
                            caloriesKcal = 242.5,
                        ),
                    ),
                ),
                onAddEntry = {},
                onDeleteEntry = {},
            )
        }

        composeTestRule.onNodeWithText("餐次汇总").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("242.5 kcal").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("蛋白 5g").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("脂肪 2.5g").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("碳水 50g").assertCountEquals(2)
        composeTestRule.onNodeWithText("米饭").assertIsDisplayed()
        composeTestRule.onNodeWithText("250g").assertIsDisplayed()
    }
}

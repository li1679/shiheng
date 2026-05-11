package com.example.nutritiontracker.feature.diary

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.diary.presentation.AddFoodEntryScreen
import com.example.nutritiontracker.feature.diary.presentation.DiaryUiState
import com.example.nutritiontracker.feature.food.data.Food
import org.junit.Rule
import org.junit.Test

class AddFoodEntryScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addFoodEntryScreenShowsFoodOptionsAndWeightField() {
        composeTestRule.setContent {
            AddFoodEntryScreen(
                uiState = DiaryUiState(
                    date = "2026-05-11",
                    mealType = MealType.LUNCH,
                    foods = listOf(
                        Food(
                            id = 1L,
                            name = "米饭",
                            imagePath = null,
                            baseWeightGram = 500.0,
                            proteinGram = 10.0,
                            fatGram = 5.0,
                            carbGram = 100.0,
                        ),
                    ),
                ),
                onFoodSelected = {},
                onActualWeightChange = {},
                onSave = {},
            )
        }

        composeTestRule.onNodeWithText("添加午餐").assertIsDisplayed()
        composeTestRule.onNodeWithText("米饭").assertIsDisplayed()
        composeTestRule.onNodeWithText("实际吃了多少（g）").assertIsDisplayed()
        composeTestRule.onNodeWithText("保存记录").assertIsDisplayed()
    }
}

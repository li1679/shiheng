package com.example.nutritiontracker.feature.food

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.feature.food.presentation.FoodEditScreen
import com.example.nutritiontracker.feature.food.presentation.FoodListScreen
import com.example.nutritiontracker.feature.food.presentation.FoodUiState
import org.junit.Rule
import org.junit.Test

class FoodListScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyFoodListShowsCreatePrompt() {
        composeTestRule.setContent {
            FoodListScreen(foods = emptyList(), onAddFood = {})
        }

        composeTestRule.onNodeWithText("先创建第一个食物").assertIsDisplayed()
    }

    @Test
    fun foodListShowsMacroSummary() {
        composeTestRule.setContent {
            FoodListScreen(
                foods = listOf(
                    Food(
                        id = 1L,
                        name = "鸡胸肉",
                        imagePath = null,
                        baseWeightGram = 500.0,
                        proteinGram = 110.0,
                        fatGram = 9.0,
                        carbGram = 0.0,
                    ),
                ),
                onAddFood = {},
            )
        }

        composeTestRule.onNodeWithText("鸡胸肉").assertIsDisplayed()
        composeTestRule.onNodeWithText("每 500g").assertIsDisplayed()
        composeTestRule.onNodeWithText("蛋白 110g").assertIsDisplayed()
        composeTestRule.onNodeWithText("脂肪 9g").assertIsDisplayed()
        composeTestRule.onNodeWithText("碳水 0g").assertIsDisplayed()
    }

    @Test
    fun foodEditShowsImageActions() {
        composeTestRule.setContent {
            FoodEditScreen(
                uiState = FoodUiState(),
                onNameChange = {},
                onBaseWeightChange = {},
                onProteinChange = {},
                onFatChange = {},
                onCarbChange = {},
                onPickImage = {},
                onTakePhoto = {},
                onSave = {},
            )
        }

        composeTestRule.onNodeWithText("食物图片").assertIsDisplayed()
        composeTestRule.onNodeWithText("从相册选择").assertIsDisplayed()
        composeTestRule.onNodeWithText("拍照").assertIsDisplayed()
    }
}

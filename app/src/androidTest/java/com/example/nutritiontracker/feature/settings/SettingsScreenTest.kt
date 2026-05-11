package com.example.nutritiontracker.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.nutritiontracker.feature.settings.presentation.SettingsScreen
import com.example.nutritiontracker.feature.settings.presentation.SettingsUiState
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsScreenShowsPreferenceFields() {
        composeTestRule.setContent {
            SettingsScreen(
                uiState = SettingsUiState(),
                onDefaultFoodBaseWeightChange = {},
                onDailyWaterGoalChange = {},
                onQuickWaterValuesChange = {},
                onThemeModeChange = {},
            )
        }

        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
        composeTestRule.onNodeWithText("默认食物基准重量（g）").assertIsDisplayed()
        composeTestRule.onNodeWithText("每日喝水目标（ml）").assertIsDisplayed()
        composeTestRule.onNodeWithText("快捷杯子").assertIsDisplayed()
        composeTestRule.onNodeWithText("250ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("500ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("新增杯子（ml）").assertIsDisplayed()
        composeTestRule.onNodeWithText("添加杯子").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("点已有杯子可删除。").assertCountEquals(0)
        composeTestRule.onNodeWithText("主题").assertIsDisplayed()
        composeTestRule.onNodeWithText("跟随系统").assertIsDisplayed()
        composeTestRule.onNodeWithText("浅色").assertIsDisplayed()
        composeTestRule.onNodeWithText("深色").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("生成备份文本").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("保存设置").assertCountEquals(0)
    }

    @Test
    fun settingsScreenDoesNotShowSavedMessage() {
        composeTestRule.setContent {
            SettingsScreen(
                uiState = SettingsUiState(successMessage = "设置已保存"),
                onDefaultFoodBaseWeightChange = {},
                onDailyWaterGoalChange = {},
                onQuickWaterValuesChange = {},
                onThemeModeChange = {},
            )
        }

        composeTestRule.onAllNodesWithText("设置已保存").assertCountEquals(0)
    }

    @Test
    fun quickWaterChipOpensDeleteConfirmDialog() {
        composeTestRule.setContent {
            var uiState by remember { mutableStateOf(SettingsUiState()) }
            SettingsScreen(
                uiState = uiState,
                onDefaultFoodBaseWeightChange = {},
                onDailyWaterGoalChange = {},
                onQuickWaterValuesChange = { value ->
                    uiState = uiState.copy(draft = uiState.draft.copy(quickWaterMlValues = value))
                },
                onQuickWaterInputChange = {},
                onAddQuickWaterValue = {},
                onRemoveQuickWaterValue = { amount ->
                    val nextValues = uiState.draft.quickWaterMlValues
                        .split(Regex("[,，\\s]+"))
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.toIntOrNull() }
                        .filterNot { it == amount }
                        .joinToString(", ")
                    uiState = uiState.copy(draft = uiState.draft.copy(quickWaterMlValues = nextValues))
                },
                onThemeModeChange = {},
            )
        }

        composeTestRule.onNodeWithText("250ml").performClick()
        composeTestRule.onNodeWithText("删除快捷杯子").assertIsDisplayed()
        composeTestRule.onNodeWithText("删除").performClick()
        composeTestRule.onAllNodesWithText("250ml").assertCountEquals(0)
    }
}

package com.example.nutritiontracker.feature.goals

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.goals.presentation.GoalTemplateDraft
import com.example.nutritiontracker.feature.goals.presentation.GoalsScreen
import com.example.nutritiontracker.feature.goals.presentation.GoalsUiState
import org.junit.Rule
import org.junit.Test

class GoalsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun goalsScreenShowsLightweightTemplateEntryAndSavedTemplates() {
        var saved = false

        composeTestRule.setContent {
            GoalsScreen(
                uiState = GoalsUiState(
                    templates = listOf(
                        GoalTemplate(id = 1L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0),
                    ),
                    templateDraft = GoalTemplateDraft(name = "休息日", proteinGoalGram = "160", fatGoalGram = "70", carbGoalGram = "180"),
                ),
                onTemplateNameChange = {},
                onProteinChange = {},
                onFatChange = {},
                onCarbChange = {},
                onSaveTemplate = {
                    saved = true
                    true
                },
                onDeleteTemplate = {},
                onOpenApply = {},
            )
        }

        composeTestRule.onNodeWithText("目标").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("模板名称").assertCountEquals(0)
        composeTestRule.onNodeWithText("新建模板").performClick()
        composeTestRule.onNodeWithText("模板名称").assertIsDisplayed()
        composeTestRule.onNodeWithText("保存").performClick()
        composeTestRule.runOnIdle {
            org.junit.Assert.assertEquals(true, saved)
        }
        composeTestRule.onNodeWithText("训练日").assertIsDisplayed()
        composeTestRule.onNodeWithText("蛋白 180g，脂肪 60g，碳水 260g").assertIsDisplayed()
        composeTestRule.onNodeWithText("应用模板").assertIsDisplayed()
    }
}

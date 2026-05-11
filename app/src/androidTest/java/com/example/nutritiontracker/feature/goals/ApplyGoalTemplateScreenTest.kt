package com.example.nutritiontracker.feature.goals

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.goals.presentation.ApplyGoalDraft
import com.example.nutritiontracker.feature.goals.presentation.ApplyGoalTemplateScreen
import com.example.nutritiontracker.feature.goals.presentation.GoalPreviewDay
import com.example.nutritiontracker.feature.goals.presentation.GoalsUiState
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class ApplyGoalTemplateScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun applyScreenShowsTemplateChoiceAndBatchFields() {
        composeTestRule.setContent {
            ApplyGoalTemplateScreen(
                uiState = GoalsUiState(
                    templates = listOf(
                        GoalTemplate(id = 1L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0),
                        GoalTemplate(id = 2L, name = "休息日", proteinGoalGram = 120.0, fatGoalGram = 50.0, carbGoalGram = 180.0),
                    ),
                    applyDraft = ApplyGoalDraft(
                        selectedTemplateId = 1L,
                        startDate = "2026-05-11",
                        dayCount = "30",
                        skippedDates = "2026-05-13",
                    ),
                    previewDates = listOf(
                        GoalPreviewDay(date = LocalDate.parse("2026-05-11"), isSkipped = false),
                        GoalPreviewDay(date = LocalDate.parse("2026-05-13"), isSkipped = true),
                    ),
                ),
                onTemplateSelected = {},
                onStartDateChange = {},
                onDayCountChange = {},
                onSkippedDatesChange = {},
                onApply = {},
            )
        }

        composeTestRule.onNodeWithText("应用目标模板").assertIsDisplayed()
        composeTestRule.onNodeWithText("训练日").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("休息日").assertCountEquals(0)
        composeTestRule.onNodeWithContentDescription("展开目标模板").performClick()
        composeTestRule.onNodeWithText("休息日").assertIsDisplayed()
        composeTestRule.onNodeWithText("开始日期").assertIsDisplayed()
        composeTestRule.onNodeWithText("应用天数").assertIsDisplayed()
        composeTestRule.onNodeWithText("跳过日期").assertIsDisplayed()
        composeTestRule.onNodeWithText("日期预览").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("应用到日历").performScrollTo().assertIsDisplayed()
    }
}

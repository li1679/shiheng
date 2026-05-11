package com.example.nutritiontracker.feature.trends

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.nutritiontracker.feature.trends.data.NutritionTrendDay
import com.example.nutritiontracker.feature.trends.data.TrendsRange
import com.example.nutritiontracker.feature.trends.data.TrendsSummary
import com.example.nutritiontracker.feature.trends.presentation.TrendsScreen
import com.example.nutritiontracker.feature.trends.presentation.TrendsUiState
import com.example.nutritiontracker.feature.weight.data.WeightLog
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class TrendsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun trendsScreenShowsDietStatisticsAndWeightLogs() {
        var previousTapped = false
        var nextTapped = false
        var selectedRange: TrendsRange? = null
        composeTestRule.setContent {
            TrendsScreen(
                uiState = TrendsUiState(
                    selectedRange = TrendsRange.WEEK,
                    anchorDate = LocalDate.parse("2026-05-11"),
                    summary = TrendsSummary(
                        range = TrendsRange.WEEK,
                        startDate = LocalDate.parse("2026-05-11"),
                        endDate = LocalDate.parse("2026-05-17"),
                        days = listOf(
                            NutritionTrendDay(
                                date = LocalDate.parse("2026-05-11"),
                                caloriesKcal = 235.0,
                                proteinGram = 8.0,
                                fatGram = 22.0,
                                carbGram = 0.0,
                                waterMl = 400,
                            ),
                            NutritionTrendDay(
                                date = LocalDate.parse("2026-05-12"),
                                caloriesKcal = 470.0,
                                proteinGram = 16.0,
                                fatGram = 44.0,
                                carbGram = 0.0,
                                waterMl = 0,
                            ),
                        ),
                        weightLogs = listOf(
                            WeightLog(date = "2026-05-11", weightKg = 80.1, note = "复称"),
                        ),
                    ),
                ),
                onRangeSelected = { selectedRange = it },
                onPreviousPeriod = { previousTapped = true },
                onNextPeriod = { nextTapped = true },
                onSelectAnchorDate = {},
                onSelectToday = {},
            )
        }

        composeTestRule.onNodeWithText("饮食统计").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("上一段").performClick()
        composeTestRule.onNodeWithContentDescription("下一段").performClick()
        composeTestRule.runOnIdle {
            org.junit.Assert.assertEquals(true, previousTapped)
            org.junit.Assert.assertEquals(true, nextTapped)
        }
        composeTestRule.onNodeWithText("本周").assertIsDisplayed()
        composeTestRule.onNodeWithText("本月").assertIsDisplayed()
        composeTestRule.onNodeWithText("今年").assertIsDisplayed()
        composeTestRule.onNodeWithText("自选").assertIsDisplayed().performClick()
        composeTestRule.runOnIdle {
            org.junit.Assert.assertEquals(TrendsRange.CUSTOM, selectedRange)
        }
        composeTestRule.onNodeWithText("352.5 kcal/天").assertIsDisplayed()
        composeTestRule.onNodeWithText("总热量 705 kcal").assertIsDisplayed()
        composeTestRule.onNodeWithText("平均蛋白 12g").assertIsDisplayed()
        composeTestRule.onNodeWithText("每日总热量").assertIsDisplayed()
        composeTestRule.onNodeWithText("每日饮水").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("体重记录").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("80.1 kg").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun monthRangeShowsCalendarOverview() {
        composeTestRule.setContent {
            TrendsScreen(
                uiState = TrendsUiState(
                    selectedRange = TrendsRange.MONTH,
                    anchorDate = LocalDate.parse("2026-05-11"),
                    summary = TrendsSummary(
                        range = TrendsRange.MONTH,
                        startDate = LocalDate.parse("2026-05-01"),
                        endDate = LocalDate.parse("2026-05-31"),
                        days = (1..31).map { day ->
                            NutritionTrendDay(
                                date = LocalDate.of(2026, 5, day),
                                caloriesKcal = if (day == 11) 235.0 else 0.0,
                                proteinGram = if (day == 11) 8.0 else 0.0,
                                fatGram = if (day == 11) 22.0 else 0.0,
                                carbGram = 0.0,
                                waterMl = if (day == 11) 400 else 0,
                            )
                        },
                    ),
                ),
            )
        }

        composeTestRule.onNodeWithText("月历概览").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("2026年5月").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("11").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("235kcal").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("水 400ml").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun customRangeShowsStartAndEndDateControls() {
        var startDate: String? = null
        var endDate: String? = null
        composeTestRule.setContent {
            TrendsScreen(
                uiState = TrendsUiState(
                    selectedRange = TrendsRange.CUSTOM,
                    anchorDate = LocalDate.parse("2026-05-11"),
                    summary = TrendsSummary(
                        range = TrendsRange.CUSTOM,
                        startDate = LocalDate.parse("2026-05-03"),
                        endDate = LocalDate.parse("2026-05-12"),
                    ),
                ),
                onSelectStartDate = { startDate = it },
                onSelectEndDate = { endDate = it },
            )
        }

        composeTestRule.onNodeWithText("5/3 - 5/12").assertIsDisplayed()
        composeTestRule.onNodeWithText("自选").assertIsDisplayed()
        composeTestRule.onNodeWithText("开始 5/3").assertIsDisplayed()
        composeTestRule.onNodeWithText("结束 5/12").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("选择开始日期").performClick()
        composeTestRule.onNodeWithText("选择日期").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("确定").performClick()
        composeTestRule.runOnIdle {
            org.junit.Assert.assertEquals("2026-05-03", startDate)
        }
        composeTestRule.onNodeWithContentDescription("选择结束日期").performClick()
        composeTestRule.onNodeWithText("选择日期").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").performClick()
        composeTestRule.onNodeWithText("确定").performClick()
        composeTestRule.runOnIdle {
            org.junit.Assert.assertEquals("2026-05-12", endDate)
        }
    }
}

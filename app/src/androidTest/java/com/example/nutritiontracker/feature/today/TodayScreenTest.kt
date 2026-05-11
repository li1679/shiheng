package com.example.nutritiontracker.feature.today

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.nutrition.MacroValues
import com.example.nutritiontracker.feature.diary.data.FoodEntry
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.today.data.DailyGoalSummary
import com.example.nutritiontracker.feature.today.data.MealSummary
import com.example.nutritiontracker.feature.today.data.TodaySummary
import com.example.nutritiontracker.feature.today.presentation.TodayGoalQuickApplyUiState
import com.example.nutritiontracker.feature.today.presentation.GoalSettingsScreen
import com.example.nutritiontracker.feature.today.presentation.MacroQuickGoalDialog
import com.example.nutritiontracker.feature.today.presentation.MacroGoalType
import com.example.nutritiontracker.feature.today.presentation.TodayScreen
import com.example.nutritiontracker.feature.today.presentation.TodayUiState
import com.example.nutritiontracker.feature.today.presentation.WaterDetailsScreen
import com.example.nutritiontracker.feature.water.data.WaterEntry
import com.example.nutritiontracker.feature.water.data.WaterLog
import com.example.nutritiontracker.feature.water.presentation.WaterUiState
import com.example.nutritiontracker.feature.weight.data.WeightLog
import com.example.nutritiontracker.feature.weight.presentation.WeightDraft
import com.example.nutritiontracker.feature.weight.presentation.WeightUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TodayScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun todayScreenShowsTotalsAndMealCounts() {
        var previousTapped = false
        var nextTapped = false
        var waterDetailsOpened = false
        var goalSettingsOpened = false
        var openedMacroGoalType: MacroGoalType? = null
        var weightDetailsOpened = false
        var weightRecordOpened = false

        composeTestRule.setContent {
            TodayScreen(
                uiState = TodayUiState(
                    summary = TodaySummary(
                        date = "2026-05-11",
                        totalMacros = MacroValues(proteinGram = 30.0, fatGram = 11.0, carbGram = 65.0),
                        caloriesKcal = 479.0,
                        dailyGoal = DailyGoalSummary(
                            date = "2026-05-11",
                            proteinGoalGram = 100.0,
                            fatGoalGram = 60.0,
                            carbGoalGram = 250.0,
                            isFreeDay = false,
                        ),
                        meals = MealType.entries.map { mealType ->
                            if (mealType == MealType.LUNCH) {
                                MealSummary(
                                    mealType = mealType,
                                    entries = listOf(
                                        FoodEntry(
                                            id = 1L,
                                            date = "2026-05-11",
                                            mealType = MealType.LUNCH,
                                            foodId = 1L,
                                            foodName = "米饭",
                                            foodImagePath = null,
                                            actualWeightGram = 250.0,
                                            macros = MacroValues(5.0, 2.5, 50.0),
                                            caloriesKcal = 242.5,
                                        ),
                                    ),
                                    totalMacros = MacroValues(5.0, 2.5, 50.0),
                                    caloriesKcal = 242.5,
                                )
                            } else {
                                MealSummary(mealType = mealType)
                            }
                        },
                    ),
                ),
                waterUiState = WaterUiState(
                    date = "2026-05-11",
                    waterLog = WaterLog(
                        date = "2026-05-11",
                        totalMl = 750,
                        entries = listOf(
                            WaterEntry(id = 10L, date = "2026-05-11", amountMl = 400, recordedAt = 100L),
                            WaterEntry(id = 11L, date = "2026-05-11", amountMl = 350, recordedAt = 200L),
                        ),
                    ),
                    dailyWaterGoalMl = 2000,
                ),
                weightUiState = WeightUiState(
                    date = "2026-05-11",
                    weightLog = WeightLog(date = "2026-05-11", weightKg = 72.4, note = "空腹"),
                    draft = WeightDraft(weightKg = "72.4", note = "空腹"),
                ),
                onMealClick = {},
                onPreviousDate = { previousTapped = true },
                onNextDate = { nextTapped = true },
                onSelectDate = {},
                onSelectToday = {},
                onQuickAddWater = {},
                onOpenWaterDetails = { waterDetailsOpened = true },
                onOpenGoalSettings = { goalSettingsOpened = true },
                onOpenMacroQuickEdit = { openedMacroGoalType = it },
                onOpenWeightDetails = { weightDetailsOpened = true },
                onOpenWeightRecord = { weightRecordOpened = true },
            )
        }

        composeTestRule.onNodeWithText("今天").assertIsDisplayed()
        composeTestRule.onNodeWithText("2026-05-11").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("前一天").performClick()
        composeTestRule.onNodeWithContentDescription("后一天").performClick()
        composeTestRule.onNodeWithText("已进食").assertIsDisplayed()
        composeTestRule.onNodeWithText("蛋白(g)").assertIsDisplayed()
        composeTestRule.onNodeWithText("30 / 100g").assertIsDisplayed()
        composeTestRule.onNodeWithText("还差 70g").assertIsDisplayed()
        composeTestRule.onNodeWithText("脂肪(g)").assertIsDisplayed()
        composeTestRule.onNodeWithText("11 / 60g").assertIsDisplayed()
        composeTestRule.onNodeWithText("还差 49g").assertIsDisplayed()
        composeTestRule.onNodeWithText("碳水(g)").assertIsDisplayed()
        composeTestRule.onNodeWithText("65 / 250g").assertIsDisplayed()
        composeTestRule.onNodeWithText("还差 185g").assertIsDisplayed()
        composeTestRule.onNodeWithText("479 kcal").assertIsDisplayed()
        composeTestRule.onNodeWithText("饮水(ml)").assertIsDisplayed()
        composeTestRule.onNodeWithText("750 / 2000ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("+250ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("+500ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("饮水").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("快速加水").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("400ml").assertCountEquals(0)
        composeTestRule.onNodeWithContentDescription("打开目标设置").performClick()
        composeTestRule.onNodeWithContentDescription("打开饮水明细").performClick()
        composeTestRule.onNodeWithContentDescription("快速设置蛋白目标").performClick()
        composeTestRule.runOnIdle {
            assertEquals(true, previousTapped)
            assertEquals(true, nextTapped)
            assertEquals(true, waterDetailsOpened)
            assertEquals(true, goalSettingsOpened)
            assertEquals(MacroGoalType.Protein, openedMacroGoalType)
        }
        composeTestRule.onNodeWithText("72.4 kg").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("空腹").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("打开体重详情").performScrollTo().performClick()
        composeTestRule.onNodeWithText("记录体重").performScrollTo().performClick()
        composeTestRule.runOnIdle {
            assertEquals(true, weightDetailsOpened)
            assertEquals(true, weightRecordOpened)
        }
        composeTestRule.onNodeWithText("午餐").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("1 条").assertIsDisplayed()
    }

    @Test
    fun macroQuickGoalDialogUsesNutritionStyledQuickControls() {
        var changedValue: String? = null
        var saved = false

        composeTestRule.setContent {
            MacroQuickGoalDialog(
                macroGoalType = MacroGoalType.Protein,
                value = "100",
                onValueChange = { changedValue = it },
                onSave = { saved = true },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("蛋白目标").assertIsDisplayed()
        composeTestRule.onNodeWithText("快速设定每日蛋白目标").assertIsDisplayed()
        composeTestRule.onNodeWithText("当前目标").assertIsDisplayed()
        composeTestRule.onNodeWithText("120g").performClick()
        composeTestRule.onNodeWithText("完成").performClick()

        composeTestRule.runOnIdle {
            assertEquals("120", changedValue)
            assertEquals(true, saved)
        }
    }

    @Test
    fun macroQuickGoalDialogDeletesCurrentGoal() {
        var deleted = false

        composeTestRule.setContent {
            MacroQuickGoalDialog(
                macroGoalType = MacroGoalType.Protein,
                value = "100",
                canDelete = true,
                onValueChange = {},
                onSave = {},
                onDelete = { deleted = true },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("删除当前目标").assertIsDisplayed().performClick()
        composeTestRule.runOnIdle {
            assertEquals(true, deleted)
        }
    }

    @Test
    fun macroQuickGoalDialogOutsideTapDismissesWithoutSaving() {
        var dismissed = false
        var saved = false

        composeTestRule.setContent {
            MacroQuickGoalDialog(
                macroGoalType = MacroGoalType.Protein,
                value = "100",
                onValueChange = {},
                onSave = { saved = true },
                onDismiss = { dismissed = true },
            )
        }

        composeTestRule.onNode(hasTestTag("nutrition-dialog-scrim") and hasClickAction(), useUnmergedTree = true)
            .assertHasClickAction()
            .performTouchInput {
                click(Offset(8f, 8f))
            }

        composeTestRule.runOnIdle {
            assertEquals(true, dismissed)
            assertEquals(false, saved)
        }
    }

    @Test
    fun waterDetailsScreenShowsEntriesAndDeletes() {
        var deletedWaterEntryId: Long? = null

        composeTestRule.setContent {
            WaterDetailsScreen(
                waterUiState = WaterUiState(
                    date = "2026-05-11",
                    waterLog = WaterLog(
                        date = "2026-05-11",
                        totalMl = 750,
                        entries = listOf(
                            WaterEntry(id = 10L, date = "2026-05-11", amountMl = 400, recordedAt = 100L),
                            WaterEntry(id = 11L, date = "2026-05-11", amountMl = 350, recordedAt = 200L),
                        ),
                    ),
                    dailyWaterGoalMl = 2000,
                ),
                onQuickAddWater = {},
                onDeleteWaterEntry = { deletedWaterEntryId = it },
            )
        }

        composeTestRule.onNodeWithText("饮水明细").assertIsDisplayed()
        composeTestRule.onNodeWithText("750 / 2000ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("400ml").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("删除饮水记录 400ml").performClick()
        composeTestRule.runOnIdle {
            assertEquals(10L, deletedWaterEntryId)
        }
    }

    @Test
    fun goalSettingsScreenKeepsTemplateListCollapsed() {
        var selectedTemplateId: Long? = null
        var applyOneDay = false
        var applyThirtyDays = false
        var manualSaved = false

        composeTestRule.setContent {
            GoalSettingsScreen(
                uiState = TodayGoalQuickApplyUiState(
                    templates = listOf(
                        GoalTemplate(id = 1L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0),
                        GoalTemplate(id = 2L, name = "休息日", proteinGoalGram = 120.0, fatGoalGram = 50.0, carbGoalGram = 180.0),
                    ),
                    selectedTemplateId = 1L,
                    selectedTemplate = GoalTemplate(id = 1L, name = "训练日", proteinGoalGram = 180.0, fatGoalGram = 60.0, carbGoalGram = 260.0),
                ),
                onManualProteinGoalChange = {},
                onManualFatGoalChange = {},
                onManualCarbGoalChange = {},
                onSaveManualGoal = { manualSaved = true },
                onGoalTemplateSelected = { selectedTemplateId = it },
                onApplyGoalToSelectedDate = { applyOneDay = true },
                onApplyGoalForNextThirtyDays = { applyThirtyDays = true },
            )
        }

        composeTestRule.onNodeWithText("选择目标模板").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("手动设置目标").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("输入蛋白目标").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("输入脂肪目标").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("输入碳水目标").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("保存这一天目标").performScrollTo().performClick()
        composeTestRule.onNodeWithText("训练日").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("休息日").assertCountEquals(0)
        composeTestRule.onNodeWithText("更换模板").performScrollTo().performClick()
        composeTestRule.onNodeWithText("休息日").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("休息日").performScrollTo().performClick()
        composeTestRule.onNodeWithText("应用到这一天").performScrollTo().performClick()
        composeTestRule.onNodeWithText("未来30天").performScrollTo().performClick()
        composeTestRule.runOnIdle {
            assertEquals(true, manualSaved)
            assertEquals(2L, selectedTemplateId)
            assertEquals(true, applyOneDay)
            assertEquals(true, applyThirtyDays)
        }
    }
}

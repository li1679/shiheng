package com.example.nutritiontracker.feature.today

import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.today.presentation.TodayPageState
import com.example.nutritiontracker.feature.today.presentation.resolveTodayPageState
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayPageStateTest {
    @Test
    fun mealEntriesStateKeepsMealTypeSnapshotWhenSelectionIsCleared() {
        val outgoingMealPage = resolveTodayPageState(
            showGoalSettings = false,
            showWaterDetails = false,
            showWeightDetails = false,
            selectedMealType = MealType.LUNCH,
            addingEntry = false,
        )

        val homePage = resolveTodayPageState(
            showGoalSettings = false,
            showWaterDetails = false,
            showWeightDetails = false,
            selectedMealType = null,
            addingEntry = false,
        )

        assertEquals(MealType.LUNCH, (outgoingMealPage as TodayPageState.MealEntries).mealType)
        assertEquals(TodayPageState.Home, homePage)
        assertEquals(MealType.LUNCH, outgoingMealPage.mealType)
    }
}

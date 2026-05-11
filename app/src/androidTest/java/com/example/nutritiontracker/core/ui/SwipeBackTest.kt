package com.example.nutritiontracker.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SwipeBackTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nutritionEdgeSwipeBackCallsCallbackFromLeftEdge() {
        var backCount = 0

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nutritionEdgeSwipeBack(enabled = true) { backCount += 1 }
                    .testTag("swipe-back-area"),
            )
        }

        composeTestRule.onNodeWithTag("swipe-back-area").performTouchInput {
            down(Offset(48f, center.y))
            advanceEventTime(16L)
            moveTo(Offset(110f, center.y))
            advanceEventTime(16L)
            moveTo(Offset(180f, center.y))
            advanceEventTime(16L)
            moveTo(Offset(260f, center.y))
            advanceEventTime(16L)
            up()
        }

        composeTestRule.runOnIdle {
            assertEquals(1, backCount)
        }
    }
}

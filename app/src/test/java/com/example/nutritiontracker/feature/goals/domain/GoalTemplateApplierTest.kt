package com.example.nutritiontracker.feature.goals.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalTemplateApplierTest {
    @Test
    fun skipsSelectedDatesWhenApplyingTemplate() {
        val dates = GoalTemplateApplier.datesToApply(
            startDate = LocalDate.of(2026, 5, 11),
            dayCount = 5,
            skippedDates = setOf(LocalDate.of(2026, 5, 13), LocalDate.of(2026, 5, 15)),
        )

        assertEquals(
            listOf(LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 14)),
            dates,
        )
    }
}

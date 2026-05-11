package com.example.nutritiontracker.core.time

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateRangeTest {
    @Test
    fun createsInclusiveForwardDatesByDayCount() {
        val dates = DateRange.forwardDates(LocalDate.of(2026, 5, 11), dayCount = 3)

        assertEquals(
            listOf(
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 12),
                LocalDate.of(2026, 5, 13),
            ),
            dates,
        )
    }
}

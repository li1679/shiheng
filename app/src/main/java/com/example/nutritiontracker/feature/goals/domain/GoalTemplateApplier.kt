package com.example.nutritiontracker.feature.goals.domain

import com.example.nutritiontracker.core.time.DateRange
import java.time.LocalDate

object GoalTemplateApplier {
    fun datesToApply(startDate: LocalDate, dayCount: Int, skippedDates: Set<LocalDate>): List<LocalDate> =
        DateRange.forwardDates(startDate, dayCount).filterNot { it in skippedDates }
}

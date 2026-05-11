package com.example.nutritiontracker.core.time

import java.time.LocalDate

object DateRange {
    fun forwardDates(startDate: LocalDate, dayCount: Int): List<LocalDate> {
        require(dayCount > 0) { "dayCount must be greater than 0" }
        return List(dayCount) { offset -> startDate.plusDays(offset.toLong()) }
    }
}

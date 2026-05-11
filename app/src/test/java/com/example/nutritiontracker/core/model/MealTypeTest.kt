package com.example.nutritiontracker.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MealTypeTest {
    @Test
    fun exposesSixMealTypesInDailyDisplayOrder() {
        assertEquals(
            listOf("早餐", "上午加餐", "午餐", "下午加餐", "晚餐", "晚上加餐"),
            MealType.entries.map { it.label },
        )
    }
}

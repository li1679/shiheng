package com.example.nutritiontracker.core.nutrition

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionCalculatorTest {
    @Test
    fun scalesMacrosFromBaseWeightToActualWeight() {
        val base = MacroValues(proteinGram = 50.0, fatGram = 20.0, carbGram = 100.0)

        val result = NutritionCalculator.scale(base = base, baseWeightGram = 500.0, actualWeightGram = 250.0)

        assertEquals(25.0, result.proteinGram, 0.001)
        assertEquals(10.0, result.fatGram, 0.001)
        assertEquals(50.0, result.carbGram, 0.001)
    }

    @Test
    fun calculatesCaloriesFromMacros() {
        val macros = MacroValues(proteinGram = 10.0, fatGram = 5.0, carbGram = 20.0)

        assertEquals(165.0, NutritionCalculator.calories(macros), 0.001)
    }
}

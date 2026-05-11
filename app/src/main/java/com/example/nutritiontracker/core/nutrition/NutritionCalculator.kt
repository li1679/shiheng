package com.example.nutritiontracker.core.nutrition

object NutritionCalculator {
    fun scale(base: MacroValues, baseWeightGram: Double, actualWeightGram: Double): MacroValues {
        require(baseWeightGram > 0.0) { "baseWeightGram must be greater than 0" }
        require(actualWeightGram > 0.0) { "actualWeightGram must be greater than 0" }

        val ratio = actualWeightGram / baseWeightGram
        return MacroValues(
            proteinGram = base.proteinGram * ratio,
            fatGram = base.fatGram * ratio,
            carbGram = base.carbGram * ratio,
        )
    }

    fun calories(macros: MacroValues): Double =
        macros.proteinGram * 4.0 + macros.fatGram * 9.0 + macros.carbGram * 4.0
}

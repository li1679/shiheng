package com.example.nutritiontracker.core.database.converter

import androidx.room.TypeConverter
import com.example.nutritiontracker.core.model.MealType

class MealTypeConverter {
    @TypeConverter
    fun toMealType(value: String): MealType = MealType.valueOf(value)

    @TypeConverter
    fun fromMealType(value: MealType): String = value.name
}

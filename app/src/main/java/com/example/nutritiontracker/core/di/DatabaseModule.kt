package com.example.nutritiontracker.core.di

import android.content.Context
import androidx.room.Room
import com.example.nutritiontracker.core.database.NutritionDatabase
import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideNutritionDatabase(@ApplicationContext context: Context): NutritionDatabase =
        Room.databaseBuilder(context, NutritionDatabase::class.java, "nutrition-tracker.db")
            .addMigrations(NutritionDatabase.MIGRATION_1_2, NutritionDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideFoodDao(database: NutritionDatabase): FoodDao = database.foodDao()

    @Provides
    fun provideFoodEntryDao(database: NutritionDatabase): FoodEntryDao = database.foodEntryDao()

    @Provides
    fun provideGoalDao(database: NutritionDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideWaterDao(database: NutritionDatabase): WaterDao = database.waterDao()

    @Provides
    fun provideWeightDao(database: NutritionDatabase): WeightDao = database.weightDao()
}

package com.example.nutritiontracker.core.di

import com.example.nutritiontracker.core.datastore.DefaultUserPreferencesRepository
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.feature.backup.data.BackupRepository
import com.example.nutritiontracker.feature.backup.data.DefaultBackupRepository
import com.example.nutritiontracker.feature.diary.data.DefaultDiaryRepository
import com.example.nutritiontracker.feature.diary.data.DiaryRepository
import com.example.nutritiontracker.feature.food.data.DefaultFoodRepository
import com.example.nutritiontracker.feature.food.data.FoodRepository
import com.example.nutritiontracker.feature.goals.data.DefaultGoalRepository
import com.example.nutritiontracker.feature.goals.data.GoalRepository
import com.example.nutritiontracker.feature.today.data.DefaultTodayRepository
import com.example.nutritiontracker.feature.today.data.TodayRepository
import com.example.nutritiontracker.feature.trends.data.DefaultTrendsRepository
import com.example.nutritiontracker.feature.trends.data.TrendsRepository
import com.example.nutritiontracker.feature.water.data.DefaultWaterRepository
import com.example.nutritiontracker.feature.water.data.WaterRepository
import com.example.nutritiontracker.feature.weight.data.DefaultWeightRepository
import com.example.nutritiontracker.feature.weight.data.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindFoodRepository(repository: DefaultFoodRepository): FoodRepository

    @Binds
    abstract fun bindDiaryRepository(repository: DefaultDiaryRepository): DiaryRepository

    @Binds
    abstract fun bindTodayRepository(repository: DefaultTodayRepository): TodayRepository

    @Binds
    abstract fun bindTrendsRepository(repository: DefaultTrendsRepository): TrendsRepository

    @Binds
    abstract fun bindGoalRepository(repository: DefaultGoalRepository): GoalRepository

    @Binds
    abstract fun bindWaterRepository(repository: DefaultWaterRepository): WaterRepository

    @Binds
    abstract fun bindWeightRepository(repository: DefaultWeightRepository): WeightRepository

    @Binds
    abstract fun bindBackupRepository(repository: DefaultBackupRepository): BackupRepository

    @Binds
    abstract fun bindUserPreferencesRepository(repository: DefaultUserPreferencesRepository): UserPreferencesRepository
}

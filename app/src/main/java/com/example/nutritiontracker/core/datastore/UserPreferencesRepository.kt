package com.example.nutritiontracker.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface UserPreferencesRepository {
    val userPreferencesStream: Flow<UserPreferences>

    suspend fun updatePreferences(preferences: UserPreferences)
}

class DefaultUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    override val userPreferencesStream: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                defaultFoodBaseWeightGram = preferences[DEFAULT_FOOD_BASE_WEIGHT_GRAM] ?: 500.0,
                dailyWaterGoalMl = preferences[DAILY_WATER_GOAL_ML] ?: 2000,
                quickWaterMlValues = preferences[QUICK_WATER_ML_VALUES]
                    ?.split(",")
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.takeIf { it.isNotEmpty() }
                    ?: listOf(250, 500),
                themeMode = preferences[THEME_MODE] ?: "system",
            )
        }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        dataStore.edit { store ->
            store[DEFAULT_FOOD_BASE_WEIGHT_GRAM] = preferences.defaultFoodBaseWeightGram
            store[DAILY_WATER_GOAL_ML] = preferences.dailyWaterGoalMl
            store[QUICK_WATER_ML_VALUES] = preferences.quickWaterMlValues.joinToString(",")
            store[THEME_MODE] = preferences.themeMode
        }
    }

    private companion object {
        val DEFAULT_FOOD_BASE_WEIGHT_GRAM = doublePreferencesKey("default_food_base_weight_gram")
        val DAILY_WATER_GOAL_ML = intPreferencesKey("daily_water_goal_ml")
        val QUICK_WATER_ML_VALUES = stringPreferencesKey("quick_water_ml_values")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}

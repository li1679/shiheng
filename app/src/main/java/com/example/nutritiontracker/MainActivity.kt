package com.example.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.core.datastore.UserPreferences
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.theme.ThemeMode
import com.example.nutritiontracker.theme.NutritionTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val preferences by userPreferencesRepository.userPreferencesStream.collectAsStateWithLifecycle(initialValue = UserPreferences())
      val systemInDarkTheme = isSystemInDarkTheme()
      NutritionTrackerTheme(darkTheme = ThemeMode.resolveDarkTheme(preferences.themeMode, systemInDarkTheme)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() }
      }
    }
  }
}

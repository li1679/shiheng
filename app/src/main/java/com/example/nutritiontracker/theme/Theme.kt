package com.example.nutritiontracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = NutritionGreenDark,
  onPrimary = NutritionBackgroundDark,
  primaryContainer = NutritionSurfaceMutedDark,
  onPrimaryContainer = NutritionInkDark,
  secondary = WaterBlue,
  secondaryContainer = NutritionSurfaceMutedDark,
  onSecondaryContainer = NutritionInkDark,
  tertiary = MacroCarb,
  background = NutritionBackgroundDark,
  onBackground = NutritionInkDark,
  surface = NutritionSurfaceDark,
  onSurface = NutritionInkDark,
  surfaceContainer = NutritionSurfaceMutedDark,
  onSurfaceVariant = NutritionMutedInkDark,
  error = NutritionError,
)

private val LightColorScheme = lightColorScheme(
  primary = NutritionGreen,
  onPrimary = NutritionSurface,
  primaryContainer = NutritionSurfaceMuted,
  onPrimaryContainer = NutritionInk,
  secondary = WaterBlue,
  secondaryContainer = NutritionSurfaceMuted,
  onSecondaryContainer = NutritionInk,
  tertiary = MacroCarb,
  background = NutritionBackground,
  onBackground = NutritionInk,
  surface = NutritionSurface,
  onSurface = NutritionInk,
  surfaceContainer = NutritionSurfaceMuted,
  onSurfaceVariant = NutritionMutedInk,
  error = NutritionError,
)

@Composable
fun NutritionTrackerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

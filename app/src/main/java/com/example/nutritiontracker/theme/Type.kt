package com.example.nutritiontracker.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFontFamily = FontFamily.Default

val Typography = Typography(
  headlineLarge = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 42.sp,
    letterSpacing = (-0.4).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.2).sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
  ),
  titleLarge = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 21.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 18.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
)

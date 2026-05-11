package com.example.nutritiontracker.theme

object ThemeMode {
    fun resolveDarkTheme(themeMode: String, systemInDarkTheme: Boolean): Boolean =
        when (themeMode) {
            "light" -> false
            "dark" -> true
            else -> systemInDarkTheme
        }
}

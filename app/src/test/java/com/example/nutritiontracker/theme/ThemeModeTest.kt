package com.example.nutritiontracker.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun systemModeFollowsSystemDarkSetting() {
        assertTrue(ThemeMode.resolveDarkTheme("system", systemInDarkTheme = true))
        assertFalse(ThemeMode.resolveDarkTheme("system", systemInDarkTheme = false))
    }

    @Test
    fun lightAndDarkModesIgnoreSystemSetting() {
        assertFalse(ThemeMode.resolveDarkTheme("light", systemInDarkTheme = true))
        assertTrue(ThemeMode.resolveDarkTheme("dark", systemInDarkTheme = false))
    }
}

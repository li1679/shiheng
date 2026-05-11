package com.example.nutritiontracker.feature.settings

import com.example.nutritiontracker.core.datastore.UserPreferences
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.feature.settings.presentation.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveSettingsStoresValidPreferences() = runTest(dispatcher) {
        val repository = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.onDefaultFoodBaseWeightChange("400")
        viewModel.onDailyWaterGoalChange("2800")
        viewModel.onQuickWaterValuesChange("200, 350, 500")
        viewModel.onThemeModeChange("dark")
        advanceUntilIdle()

        assertEquals(
            UserPreferences(
                defaultFoodBaseWeightGram = 400.0,
                dailyWaterGoalMl = 2800,
                quickWaterMlValues = listOf(200, 350, 500),
                themeMode = "dark",
            ),
            repository.saved.last(),
        )
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun saveSettingsRejectsInvalidQuickWaterValues() = runTest(dispatcher) {
        val repository = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.onQuickWaterValuesChange("250, 0")
        advanceUntilIdle()

        assertTrue(repository.saved.isEmpty())
        assertEquals("快捷饮水值必须大于 0", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun addAndRemoveQuickWaterCupUpdatesDraft() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(FakeUserPreferencesRepository())
        advanceUntilIdle()

        viewModel.onQuickWaterInputChange("300")
        assertTrue(viewModel.addQuickWaterValue())
        viewModel.removeQuickWaterValue(250)
        advanceUntilIdle()

        assertEquals("500, 300", viewModel.uiState.value.draft.quickWaterMlValues)
        assertEquals("", viewModel.uiState.value.draft.quickWaterInput)
    }

    @Test
    fun addQuickWaterCupRejectsInvalidInput() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(FakeUserPreferencesRepository())

        viewModel.onQuickWaterInputChange("abc")
        assertFalse(viewModel.addQuickWaterValue())
        advanceUntilIdle()

        assertEquals("新增杯子必须填写数字", viewModel.uiState.value.errorMessage)
    }
}

private class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences(),
) : UserPreferencesRepository {
    private val preferences = MutableStateFlow(initial)
    val saved = mutableListOf<UserPreferences>()

    override val userPreferencesStream: Flow<UserPreferences> = preferences

    override suspend fun updatePreferences(preferences: UserPreferences) {
        saved += preferences
        this.preferences.value = preferences
    }
}

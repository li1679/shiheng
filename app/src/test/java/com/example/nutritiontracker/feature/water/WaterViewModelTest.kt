package com.example.nutritiontracker.feature.water

import com.example.nutritiontracker.core.datastore.UserPreferences
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.feature.water.data.WaterEntry
import com.example.nutritiontracker.feature.water.data.WaterLog
import com.example.nutritiontracker.feature.water.data.WaterRepository
import com.example.nutritiontracker.feature.water.presentation.WaterViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WaterViewModelTest {
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
    fun quickAddUsesSelectedDate() = runTest(dispatcher) {
        val repository = FakeWaterRepository()
        val viewModel = WaterViewModel(repository, FakeUserPreferencesRepository())

        viewModel.selectDate("2026-05-11")
        viewModel.quickAdd(250)
        advanceUntilIdle()

        assertEquals(WaterAddCall("2026-05-11", 250), repository.addCalls.single())
    }

    @Test
    fun deleteEntryDelegatesToRepository() = runTest(dispatcher) {
        val repository = FakeWaterRepository(
            WaterLog(
                date = "2026-05-11",
                totalMl = 450,
                entries = listOf(WaterEntry(id = 7L, date = "2026-05-11", amountMl = 450, recordedAt = 100L)),
            ),
        )
        val viewModel = WaterViewModel(repository, FakeUserPreferencesRepository())

        viewModel.deleteEntry(7L)
        advanceUntilIdle()

        assertEquals(listOf(7L), repository.deletedEntryIds)
    }

    @Test
    fun quickAddRejectsNonPositiveAmount() = runTest(dispatcher) {
        val repository = FakeWaterRepository()
        val viewModel = WaterViewModel(repository, FakeUserPreferencesRepository())

        viewModel.quickAdd(0)
        advanceUntilIdle()

        assertTrue(repository.addCalls.isEmpty())
        assertEquals("饮水量必须大于 0", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun uiStateUsesWaterGoalAndQuickValuesFromPreferences() = runTest(dispatcher) {
        val viewModel = WaterViewModel(
            repository = FakeWaterRepository(),
            userPreferencesRepository = FakeUserPreferencesRepository(
                UserPreferences(
                    dailyWaterGoalMl = 3000,
                    quickWaterMlValues = listOf(300, 600),
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(3000, viewModel.uiState.value.dailyWaterGoalMl)
        assertEquals(listOf(300, 600), viewModel.uiState.value.quickAddMlValues)
    }
}

private class FakeWaterRepository(initial: WaterLog = WaterLog(date = "2026-05-11", totalMl = 0)) : WaterRepository {
    private val waterLog = MutableStateFlow(initial)
    val addCalls = mutableListOf<WaterAddCall>()
    val deletedEntryIds = mutableListOf<Long>()

    override fun getWaterLogStream(date: String): Flow<WaterLog> = waterLog

    override suspend fun addWater(date: String, amountMl: Int): Long {
        addCalls += WaterAddCall(date, amountMl)
        return addCalls.size.toLong()
    }

    override suspend fun deleteWaterEntry(id: Long) {
        deletedEntryIds += id
    }
}

private data class WaterAddCall(val date: String, val amountMl: Int)

private class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences(),
) : UserPreferencesRepository {
    override val userPreferencesStream: Flow<UserPreferences> = MutableStateFlow(initial)

    override suspend fun updatePreferences(preferences: UserPreferences) = Unit
}

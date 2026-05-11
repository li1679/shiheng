package com.example.nutritiontracker.feature.weight

import com.example.nutritiontracker.feature.weight.data.WeightLog
import com.example.nutritiontracker.feature.weight.data.WeightRepository
import com.example.nutritiontracker.feature.weight.presentation.WeightViewModel
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
class WeightViewModelTest {
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
    fun saveWeightStoresSelectedDateWeightAndNote() = runTest(dispatcher) {
        val repository = FakeWeightRepository()
        val viewModel = WeightViewModel(repository)

        viewModel.selectDate("2026-05-11")
        viewModel.onWeightChange("80.5")
        viewModel.onNoteChange("空腹")
        viewModel.saveWeight()
        advanceUntilIdle()

        assertEquals(WeightSaveCall("2026-05-11", 80.5, "空腹"), repository.saveCalls.single())
    }

    @Test
    fun saveWeightRejectsNonPositiveWeight() = runTest(dispatcher) {
        val repository = FakeWeightRepository()
        val viewModel = WeightViewModel(repository)

        viewModel.onWeightChange("0")
        viewModel.saveWeight()
        advanceUntilIdle()

        assertTrue(repository.saveCalls.isEmpty())
        assertEquals("体重必须大于 0", viewModel.uiState.value.errorMessage)
    }
}

private class FakeWeightRepository : WeightRepository {
    private val weightLog = MutableStateFlow<WeightLog?>(null)
    val saveCalls = mutableListOf<WeightSaveCall>()

    override fun getWeightLogStream(date: String): Flow<WeightLog?> = weightLog

    override fun getWeightLogsStream(): Flow<List<WeightLog>> = MutableStateFlow(emptyList())

    override suspend fun saveWeight(date: String, weightKg: Double, note: String?) {
        saveCalls += WeightSaveCall(date, weightKg, note)
    }
}

private data class WeightSaveCall(val date: String, val weightKg: Double, val note: String?)

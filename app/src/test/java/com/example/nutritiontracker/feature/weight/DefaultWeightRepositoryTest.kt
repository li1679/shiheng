package com.example.nutritiontracker.feature.weight

import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import com.example.nutritiontracker.feature.weight.data.DefaultWeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DefaultWeightRepositoryTest {
    @Test
    fun saveWeightTwiceOnSameDateUpdatesSingleRowAndKeepsNote() = runTest {
        val weightDao = FakeWeightDao()
        val repository = DefaultWeightRepository(weightDao)

        repository.saveWeight(date = "2026-05-11", weightKg = 80.5, note = "空腹")
        repository.saveWeight(date = "2026-05-11", weightKg = 80.1, note = "复称")

        assertEquals(1, weightDao.logs.value.size)
        assertEquals(80.1, weightDao.logs.value["2026-05-11"]?.weightKg ?: 0.0, 0.001)
        assertEquals("复称", weightDao.logs.value["2026-05-11"]?.note)
    }

    @Test
    fun saveWeightRejectsNonPositiveWeight() = runTest {
        val repository = DefaultWeightRepository(FakeWeightDao())

        try {
            repository.saveWeight(date = "2026-05-11", weightKg = 0.0, note = null)
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }
}

private class FakeWeightDao : WeightDao {
    val logs = MutableStateFlow<Map<String, WeightLogEntity>>(emptyMap())

    override fun getWeightLogsStream(): Flow<List<WeightLogEntity>> =
        MutableStateFlow(logs.value.values.toList())

    override fun getWeightLogsBetweenDatesStream(startDate: String, endDate: String): Flow<List<WeightLogEntity>> =
        MutableStateFlow(logs.value.values.filter { it.date in startDate..endDate })

    override fun getWeightLogStream(date: String): Flow<WeightLogEntity?> =
        MutableStateFlow(logs.value[date])

    override suspend fun upsertWeightLog(log: WeightLogEntity) {
        logs.value = logs.value + (log.date to log)
    }

    override suspend fun getAllWeightLogsOnce(): List<WeightLogEntity> = logs.value.values.toList()
}

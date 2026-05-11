package com.example.nutritiontracker.feature.water

import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import com.example.nutritiontracker.feature.water.data.DefaultWaterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DefaultWaterRepositoryTest {
    @Test
    fun addWaterCreatesSeparateEntriesAndSumsThemForDate() = runTest {
        val waterDao = FakeWaterDao()
        val repository = DefaultWaterRepository(waterDao)

        repository.addWater(date = "2026-05-11", amountMl = 250)
        repository.addWater(date = "2026-05-11", amountMl = 500)

        val log = repository.getWaterLogStream("2026-05-11").first()

        assertEquals(750, log.totalMl)
        assertEquals(listOf(500, 250), log.entries.map { it.amountMl })
    }

    @Test
    fun addWaterRejectsNonPositiveAmount() = runTest {
        val repository = DefaultWaterRepository(FakeWaterDao())

        try {
            repository.addWater(date = "2026-05-11", amountMl = 0)
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun deleteWaterEntryRemovesOnlyThatDrink() = runTest {
        val waterDao = FakeWaterDao()
        val repository = DefaultWaterRepository(waterDao)
        val firstId = repository.addWater(date = "2026-05-11", amountMl = 200)
        repository.addWater(date = "2026-05-11", amountMl = 300)

        repository.deleteWaterEntry(firstId)

        val log = repository.getWaterLogStream("2026-05-11").first()
        assertEquals(300, log.totalMl)
        assertEquals(listOf(300), log.entries.map { it.amountMl })
    }
}

private class FakeWaterDao : WaterDao {
    val entries = MutableStateFlow<List<WaterEntryEntity>>(emptyList())
    private var nextId = 1L

    override fun getWaterEntriesStream(date: String): Flow<List<WaterEntryEntity>> =
        entries.map { values -> values.filter { it.date == date }.sortedWith(compareByDescending<WaterEntryEntity> { it.recordedAt }.thenByDescending { it.id }) }

    override fun getWaterEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<WaterEntryEntity>> =
        entries.map { values -> values.filter { it.date in startDate..endDate }.sortedWith(compareBy<WaterEntryEntity> { it.date }.thenBy { it.recordedAt }) }

    override fun getWaterTotalMlStream(date: String): Flow<Int> =
        entries.map { values -> values.filter { it.date == date }.sumOf { it.amountMl } }

    override suspend fun getAllWaterEntriesOnce(): List<WaterEntryEntity> = entries.value

    override suspend fun insertWaterEntry(entry: WaterEntryEntity): Long {
        val id = nextId++
        entries.value = entries.value + entry.copy(id = id, recordedAt = id)
        return id
    }

    override suspend fun deleteWaterEntry(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }
}

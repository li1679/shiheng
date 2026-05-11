package com.example.nutritiontracker.feature.water.data

import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class WaterEntry(
    val id: Long,
    val date: String,
    val amountMl: Int,
    val recordedAt: Long,
)

data class WaterLog(
    val date: String,
    val totalMl: Int,
    val entries: List<WaterEntry> = emptyList(),
)

interface WaterRepository {
    fun getWaterLogStream(date: String): Flow<WaterLog>

    suspend fun addWater(date: String, amountMl: Int): Long

    suspend fun deleteWaterEntry(id: Long)
}

class DefaultWaterRepository @Inject constructor(
    private val waterDao: WaterDao,
) : WaterRepository {
    override fun getWaterLogStream(date: String): Flow<WaterLog> =
        waterDao.getWaterEntriesStream(date).map { entries ->
            WaterLog(
                date = date,
                totalMl = entries.sumOf { it.amountMl },
                entries = entries.map { it.toWaterEntry() },
            )
        }

    override suspend fun addWater(date: String, amountMl: Int): Long {
        require(amountMl > 0) { "amountMl must be greater than 0" }
        return waterDao.insertWaterEntry(
            WaterEntryEntity(
                date = date,
                amountMl = amountMl,
                recordedAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun deleteWaterEntry(id: Long) {
        waterDao.deleteWaterEntry(id)
    }
}

private fun WaterEntryEntity.toWaterEntry(): WaterEntry = WaterEntry(
    id = id,
    date = date,
    amountMl = amountMl,
    recordedAt = recordedAt,
)

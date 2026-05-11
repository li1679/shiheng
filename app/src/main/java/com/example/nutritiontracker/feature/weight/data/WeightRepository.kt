package com.example.nutritiontracker.feature.weight.data

import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class WeightLog(
    val date: String,
    val weightKg: Double,
    val note: String?,
)

interface WeightRepository {
    fun getWeightLogStream(date: String): Flow<WeightLog?>

    fun getWeightLogsStream(): Flow<List<WeightLog>>

    suspend fun saveWeight(date: String, weightKg: Double, note: String?)
}

class DefaultWeightRepository @Inject constructor(
    private val weightDao: WeightDao,
) : WeightRepository {
    override fun getWeightLogStream(date: String): Flow<WeightLog?> =
        weightDao.getWeightLogStream(date).map { it?.toWeightLog() }

    override fun getWeightLogsStream(): Flow<List<WeightLog>> =
        weightDao.getWeightLogsStream().map { logs -> logs.map { it.toWeightLog() } }

    override suspend fun saveWeight(date: String, weightKg: Double, note: String?) {
        require(weightKg > 0.0) { "weightKg must be greater than 0" }
        val existing = weightDao.getWeightLogStream(date).first()
        val now = System.currentTimeMillis()
        weightDao.upsertWeightLog(
            WeightLogEntity(
                date = date,
                weightKg = weightKg,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                recordedAt = existing?.recordedAt ?: now,
                updatedAt = now,
            ),
        )
    }
}

private fun WeightLogEntity.toWeightLog(): WeightLog = WeightLog(
    date = date,
    weightKg = weightKg,
    note = note,
)

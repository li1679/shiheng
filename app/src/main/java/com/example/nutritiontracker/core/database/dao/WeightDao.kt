package com.example.nutritiontracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nutritiontracker.core.database.entity.WeightLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY date ASC")
    fun getWeightLogsStream(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeightLogsBetweenDatesStream(startDate: String, endDate: String): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE date = :date")
    fun getWeightLogStream(date: String): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs ORDER BY date ASC")
    suspend fun getAllWeightLogsOnce(): List<WeightLogEntity>

    @Upsert
    suspend fun upsertWeightLog(log: WeightLogEntity)
}

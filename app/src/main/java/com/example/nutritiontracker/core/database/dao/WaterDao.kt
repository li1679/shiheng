package com.example.nutritiontracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY recordedAt DESC, id DESC")
    fun getWaterEntriesStream(date: String): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, recordedAt ASC")
    fun getWaterEntriesBetweenDatesStream(startDate: String, endDate: String): Flow<List<WaterEntryEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE date = :date")
    fun getWaterTotalMlStream(date: String): Flow<Int>

    @Query("SELECT * FROM water_entries ORDER BY date ASC, recordedAt ASC")
    suspend fun getAllWaterEntriesOnce(): List<WaterEntryEntity>

    @Insert
    suspend fun insertWaterEntry(entry: WaterEntryEntity): Long

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteWaterEntry(id: Long)
}

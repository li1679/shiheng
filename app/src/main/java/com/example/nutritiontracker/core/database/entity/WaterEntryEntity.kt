package com.example.nutritiontracker.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_entries",
    indices = [Index("date")],
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,
    val amountMl: Int,
    val recordedAt: Long,
)

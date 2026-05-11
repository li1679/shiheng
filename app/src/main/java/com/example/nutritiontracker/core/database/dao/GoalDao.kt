package com.example.nutritiontracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goal_templates ORDER BY name COLLATE NOCASE ASC")
    fun getGoalTemplatesStream(): Flow<List<GoalTemplateEntity>>

    @Query("SELECT * FROM daily_goals WHERE date = :date")
    fun getDailyGoalStream(date: String): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goals WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getDailyGoalsBetweenDatesStream(startDate: String, endDate: String): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM goal_templates ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllGoalTemplatesOnce(): List<GoalTemplateEntity>

    @Query("SELECT * FROM daily_goals ORDER BY date ASC")
    suspend fun getAllDailyGoalsOnce(): List<DailyGoalEntity>

    @Upsert
    suspend fun upsertGoalTemplate(template: GoalTemplateEntity)

    @Upsert
    suspend fun upsertDailyGoal(goal: DailyGoalEntity)

    @Query("DELETE FROM goal_templates WHERE id = :id")
    suspend fun deleteGoalTemplate(id: Long)
}

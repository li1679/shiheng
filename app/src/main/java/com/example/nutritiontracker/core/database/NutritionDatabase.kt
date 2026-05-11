package com.example.nutritiontracker.core.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nutritiontracker.core.database.converter.MealTypeConverter
import com.example.nutritiontracker.core.database.dao.FoodDao
import com.example.nutritiontracker.core.database.dao.FoodEntryDao
import com.example.nutritiontracker.core.database.dao.GoalDao
import com.example.nutritiontracker.core.database.dao.WaterDao
import com.example.nutritiontracker.core.database.dao.WeightDao
import com.example.nutritiontracker.core.database.entity.DailyGoalEntity
import com.example.nutritiontracker.core.database.entity.FoodEntity
import com.example.nutritiontracker.core.database.entity.FoodEntryEntity
import com.example.nutritiontracker.core.database.entity.GoalTemplateEntity
import com.example.nutritiontracker.core.database.entity.WaterEntryEntity
import com.example.nutritiontracker.core.database.entity.WaterLogEntity
import com.example.nutritiontracker.core.database.entity.WeightLogEntity

@Database(
    entities = [
        FoodEntity::class,
        FoodEntryEntity::class,
        DailyGoalEntity::class,
        GoalTemplateEntity::class,
        WaterLogEntity::class,
        WaterEntryEntity::class,
        WeightLogEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(MealTypeConverter::class)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun goalDao(): GoalDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `water_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `amountMl` INTEGER NOT NULL,
                        `recordedAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_water_entries_date` ON `water_entries` (`date`)")
                db.execSQL(
                    """
                    INSERT INTO `water_entries` (`date`, `amountMl`, `recordedAt`)
                    SELECT `date`, `totalMl`, `updatedAt`
                    FROM `water_logs`
                    WHERE `totalMl` > 0
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `lastLoggedAt` INTEGER")
                db.execSQL("ALTER TABLE `foods` ADD COLUMN `logCount` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}

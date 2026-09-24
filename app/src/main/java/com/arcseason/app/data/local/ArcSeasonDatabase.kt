package com.arcseason.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arcseason.app.data.local.dao.ArcRuleDao
import com.arcseason.app.data.local.dao.NutritionDao
import com.arcseason.app.data.local.dao.ScheduleDao
import com.arcseason.app.data.local.entity.ActiveScheduleDayEntity
import com.arcseason.app.data.local.entity.ActivitySessionEntity
import com.arcseason.app.data.local.entity.ChallengeLogEntity
import com.arcseason.app.data.local.entity.DailyRuleLogEntity
import com.arcseason.app.data.local.entity.JournalEntryEntity
import com.arcseason.app.data.local.entity.NutritionLogEntity
import com.arcseason.app.data.local.entity.SchedulePresetEntity
import com.arcseason.app.data.local.entity.ScheduleBlockEntity
import com.arcseason.app.data.local.entity.UserProfileEntity

@Database(
    entities = [
        DailyRuleLogEntity::class,
        ActivitySessionEntity::class,
        JournalEntryEntity::class,
        ChallengeLogEntity::class,
        NutritionLogEntity::class,
        UserProfileEntity::class,
        SchedulePresetEntity::class,
        ScheduleBlockEntity::class,
        ActiveScheduleDayEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ArcSeasonDatabase : RoomDatabase() {
    abstract fun arcRuleDao(): ArcRuleDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: ArcSeasonDatabase? = null

        fun getInstance(context: Context): ArcSeasonDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ArcSeasonDatabase::class.java,
                    "arc_season.db"
                ).build().also { INSTANCE = it }
            }
    }
}

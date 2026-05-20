package com.steptracker.nativeapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [DailyData::class, ActivityRecord::class, Achievement::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StepTrackerDatabase : RoomDatabase() {
    abstract fun dailyDataDao(): DailyDataDao
    abstract fun activityDao(): ActivityDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: StepTrackerDatabase? = null

        fun getDatabase(context: Context): StepTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StepTrackerDatabase::class.java,
                    "steptracker_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize database with default data
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
}

package com.steptracker.nativeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyDataDao {
    @Query("SELECT * FROM daily_data WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DailyData?

    @Query("SELECT * FROM daily_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyData>>

    @Query("SELECT * FROM daily_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getRangeSync(startDate: LocalDate, endDate: LocalDate): List<DailyData>

    @Query("SELECT * FROM daily_data ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DailyData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: DailyData)

    @Update
    suspend fun update(data: DailyData)

    @Query("UPDATE daily_data SET currentSteps = currentSteps + :steps WHERE date = :date")
    suspend fun addSteps(date: LocalDate, steps: Int)

    @Query("SELECT SUM(currentSteps) as total FROM daily_data")
    suspend fun getTotalSteps(): Int?

    @Query("SELECT SUM(km) as total FROM daily_data")
    suspend fun getTotalDistance(): Double?

    @Query("SELECT SUM(kcal) as total FROM daily_data")
    suspend fun getTotalCalories(): Int?

    @Query("SELECT COUNT(*) FROM daily_data WHERE currentSteps >= dailyGoal")
    suspend fun getGoalAchievementCount(): Int
}

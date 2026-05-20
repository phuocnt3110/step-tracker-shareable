package com.steptracker.nativeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY category, threshold")
    fun getAll(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE category = :category")
    fun getByCategory(category: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE unlocked = 1")
    suspend fun getUnlocked(): List<Achievement>

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("UPDATE achievements SET unlocked = 1, unlockedDate = :date, current = :current WHERE id = :id AND unlocked = 0")
    suspend fun unlock(id: String, date: java.time.LocalDate, current: Int): Int

    @Query("UPDATE achievements SET current = :current WHERE id = :id")
    suspend fun updateProgress(id: String, current: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>)
}

package com.steptracker.nativeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY startTime DESC")
    fun getAll(): Flow<List<ActivityRecord>>
    
    @Query("SELECT * FROM activities ORDER BY startTime DESC")
    suspend fun getAllSync(): List<ActivityRecord>
    
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): ActivityRecord?
    
    @Query("SELECT * FROM activities WHERE date = :date")
    fun getByDate(date: LocalDate): Flow<List<ActivityRecord>>
    
    @Query("SELECT * FROM activities WHERE date BETWEEN :start AND :end ORDER BY startTime DESC")
    fun getByRange(start: LocalDate, end: LocalDate): Flow<List<ActivityRecord>>
    
    @Query("SELECT * FROM activities WHERE type = :type")
    fun getByType(type: String): Flow<List<ActivityRecord>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityRecord): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityRecord>)
    
    @Update
    suspend fun update(activity: ActivityRecord)
    
    @Delete
    suspend fun delete(activity: ActivityRecord)
    
    @Query("DELETE FROM activities")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM activities")
    fun getCount(): Flow<Int>
    
    @Query("SELECT SUM(steps) FROM activities WHERE date = :date")
    fun getTotalStepsForDate(date: LocalDate): Flow<Int?>
    
    @Query("SELECT SUM(kcal) FROM activities WHERE date = :date")
    fun getTotalCaloriesForDate(date: LocalDate): Flow<Int?>
    
    @Query("SELECT SUM(km) FROM activities WHERE date = :date")
    fun getTotalDistanceForDate(date: LocalDate): Flow<Float?>
}

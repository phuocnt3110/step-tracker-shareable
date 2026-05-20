package com.steptracker.nativeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun get(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSync(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)

    @Update
    suspend fun update(settings: UserSettings)

    @Query("UPDATE user_settings SET dailyGoal = :goal WHERE id = 1")
    suspend fun updateDailyGoal(goal: Int)

    @Query("UPDATE user_settings SET userName = :name WHERE id = 1")
    suspend fun updateUserName(name: String)

    @Query("UPDATE user_settings SET weight = :weight WHERE id = 1")
    suspend fun updateWeight(weight: Double)

    @Query("UPDATE user_settings SET language = :language WHERE id = 1")
    suspend fun updateLanguage(language: String)
}

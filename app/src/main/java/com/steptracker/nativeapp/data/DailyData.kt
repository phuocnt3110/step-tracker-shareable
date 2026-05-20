package com.steptracker.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "daily_data")
data class DailyData(
    @PrimaryKey
    val date: LocalDate,
    val currentSteps: Int = 0,
    val dailyGoal: Int = 8000,
    val activeMinutes: Int = 0,
    val kcal: Int = 0,
    val km: Double = 0.0,
    val weight: Double = 70.0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "activities")
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val durationMinutes: Int = 0,
    val steps: Int = 0,
    val kcal: Int = 0,
    val km: Double = 0.0,
    val type: String = "walking", // walking, running, cycling
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val highestSpeed: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val coordinatesJson: String = "[]" // JSON array of tracked coordinates
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String, // dailySteps, weeklySteps, consecutiveDays, totalDistance, caloriesBurned
    val threshold: Int,
    val unlocked: Boolean = false,
    val unlockedDate: LocalDate? = null,
    val current: Int = 0,
    val icon: String,
    val color: String
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "User",
    val dailyGoal: Int = 8000,
    val weight: Double = 70.0,
    val height: Double = 170.0,
    val language: String = "en",
    val notificationsEnabled: Boolean = true
)

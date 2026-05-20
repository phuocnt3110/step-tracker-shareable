package com.steptracker.nativeapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DataRepository(context: Context) {
    private val db = StepTrackerDatabase.getDatabase(context)
    private val dailyDataDao = db.dailyDataDao()
    private val activityDao = db.activityDao()
    private val achievementDao = db.achievementDao()
    private val userSettingsDao = db.userSettingsDao()

    // Daily Data
    fun getTodayData(): Flow<List<DailyData>> {
        val today = LocalDate.now()
        return dailyDataDao.getRange(today, today)
    }
    
    fun getYearlyData(): Flow<List<DailyData>> {
        val today = LocalDate.now()
        val yearAgo = today.minusDays(364)
        return dailyDataDao.getRange(yearAgo, today)
    }

    suspend fun getOrCreateTodayData(): DailyData = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        dailyDataDao.getByDate(today) ?: DailyData(date = today).also {
            dailyDataDao.insert(it)
        }
    }

    fun getWeeklyData(): Flow<List<DailyData>> {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(6)
        return dailyDataDao.getRange(weekAgo, today)
    }

    fun getMonthlyData(): Flow<List<DailyData>> {
        val today = LocalDate.now()
        val monthAgo = today.minusDays(29)
        return dailyDataDao.getRange(monthAgo, today)
    }

    suspend fun updateSteps(date: LocalDate, steps: Int) = withContext(Dispatchers.IO) {
        val data = dailyDataDao.getByDate(date)
        if (data != null) {
            dailyDataDao.update(data.copy(currentSteps = steps))
        } else {
            dailyDataDao.insert(DailyData(date = date, currentSteps = steps))
        }
    }

    suspend fun addSteps(steps: Int) = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        dailyDataDao.addSteps(today, steps)
        checkAchievements()
    }

    suspend fun updateWeight(weight: Double) = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val data = dailyDataDao.getByDate(today)
        if (data != null) {
            dailyDataDao.update(data.copy(weight = weight))
        } else {
            dailyDataDao.insert(DailyData(date = today, weight = weight))
        }
        userSettingsDao.updateWeight(weight)
    }

    // Activities
    fun getActivities(): Flow<List<ActivityRecord>> = activityDao.getAll()

    suspend fun getActivitiesSync(): List<ActivityRecord> = withContext(Dispatchers.IO) {
        activityDao.getAllSync()
    }

    fun getActivitiesByRange(start: LocalDate, end: LocalDate): Flow<List<ActivityRecord>> {
        return activityDao.getByRange(start, end)
    }

    suspend fun startActivity(type: String = "walking"): Long = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val now = java.time.LocalDateTime.now()
        val activity = ActivityRecord(
            date = today,
            startTime = now,
            type = type
        )
        activityDao.insert(activity)
    }

    suspend fun endActivity(id: Long, steps: Int, kcal: Int, km: Double, 
                           coordinates: List<Pair<Double, Double>>, 
                           maxSpeed: Double, avgSpeed: Double) = withContext(Dispatchers.IO) {
        val activity = activityDao.getById(id) ?: return@withContext
        val now = java.time.LocalDateTime.now()
        val duration = ChronoUnit.MINUTES.between(activity.startTime, now).toInt()
        
        val coordsJson = coordinates.joinToString(",", "[", "]") { "[${it.first},${it.second}]" }
        
        val updated = activity.copy(
            endTime = now,
            durationMinutes = duration,
            steps = steps,
            kcal = kcal,
            km = km,
            coordinatesJson = coordsJson,
            highestSpeed = maxSpeed,
            averageSpeed = avgSpeed
        )
        activityDao.update(updated)
        
        // Update today's daily data
        val today = LocalDate.now()
        val todayData = dailyDataDao.getByDate(today)
        if (todayData != null) {
            dailyDataDao.update(todayData.copy(
                currentSteps = todayData.currentSteps + steps,
                kcal = todayData.kcal + kcal,
                km = todayData.km + km,
                activeMinutes = todayData.activeMinutes + duration
            ))
        }
        checkAchievements()
    }

    suspend fun getActivity(id: Long): ActivityRecord? = withContext(Dispatchers.IO) {
        activityDao.getById(id)
    }

    suspend fun insertActivity(activity: ActivityRecord) = withContext(Dispatchers.IO) {
        activityDao.insert(activity)
    }

    suspend fun deleteActivity(activity: ActivityRecord) = withContext(Dispatchers.IO) {
        activityDao.delete(activity)
    }

    suspend fun clearAllActivities() = withContext(Dispatchers.IO) {
        activityDao.deleteAll()
    }

    // Achievements
    fun getAchievements(): Flow<List<Achievement>> = achievementDao.getAll()

    fun getAchievementsByCategory(category: String): Flow<List<Achievement>> = achievementDao.getByCategory(category)

    suspend fun getUnlockedCount(): Int = withContext(Dispatchers.IO) {
        achievementDao.getUnlockedCount()
    }

    suspend fun initializeAchievements() = withContext(Dispatchers.IO) {
        val achievements = listOf(
            // Daily Steps - Beginner
            Achievement("first_steps", "First Steps", "Walk 1,000 steps in a day", "dailySteps", 1000, icon = "footprints", color = "#10b981"),
            Achievement("morning_walker", "Morning Walker", "Walk 2,500 steps before noon", "dailySteps", 2500, icon = "sun", color = "#fbbf24"),
            Achievement("goal_setter", "Goal Setter", "Walk 5,000 steps in a day", "dailySteps", 5000, icon = "target", color = "#3b82f6"),
            Achievement("energizer", "Energizer", "Walk 10,000 steps in a day", "dailySteps", 10000, icon = "zap", color = "#f59e0b"),
            Achievement("fire_walker", "Fire Walker", "Walk 15,000 steps in a day", "dailySteps", 15000, icon = "flame", color = "#ef4444"),
            Achievement("champion", "Champion", "Walk 20,000 steps in a day", "dailySteps", 20000, icon = "trophy", color = "#8b5cf6"),
            Achievement("step_master", "Step Master", "Walk 30,000 steps in a day", "dailySteps", 30000, icon = "crown", color = "#ec4899"),
            Achievement("ultra_walker", "Ultra Walker", "Walk 50,000 steps in a day", "dailySteps", 50000, icon = "award", color = "#7c3aed"),
            
            // Total Steps - Cumulative
            Achievement("total_10k", "Getting Started", "Walk 10,000 steps total", "totalSteps", 10000, icon = "footprints", color = "#22c55e"),
            Achievement("total_50k", "50K Club", "Walk 50,000 steps total", "totalSteps", 50000, icon = "star", color = "#3b82f6"),
            Achievement("total_100k", "100K Steps", "Walk 100,000 steps total", "totalSteps", 100000, icon = "medal", color = "#f59e0b"),
            Achievement("total_500k", "Half Million", "Walk 500,000 steps total", "totalSteps", 500000, icon = "trophy", color = "#ef4444"),
            Achievement("total_1m", "Millionaire", "Walk 1,000,000 steps total", "totalSteps", 1000000, icon = "crown", color = "#8b5cf6"),
            Achievement("total_5m", "5M Legend", "Walk 5,000,000 steps total", "totalSteps", 5000000, icon = "gem", color = "#ec4899"),
            
            // Weekly Steps
            Achievement("week_warrior", "Week Warrior", "Walk 50,000 steps in a week", "weeklySteps", 50000, icon = "star", color = "#06b6d4"),
            Achievement("week_champion", "Weekly Champion", "Walk 70,000 steps in a week", "weeklySteps", 70000, icon = "award", color = "#3b82f6"),
            Achievement("marathoner", "Marathoner", "Walk 100,000 steps in a week", "weeklySteps", 100000, icon = "medal", color = "#14b8a6"),
            Achievement("week_legend", "Weekly Legend", "Walk 150,000 steps in a week", "weeklySteps", 150000, icon = "crown", color = "#8b5cf6"),
            
            // Consecutive Days (Streaks)
            Achievement("consistent_3", "3 Day Streak", "Reach your goal for 3 days in a row", "consecutiveDays", 3, icon = "calendar", color = "#f97316"),
            Achievement("consistent_5", "5 Day Streak", "Reach your goal for 5 days in a row", "consecutiveDays", 5, icon = "calendar-check", color = "#fb923c"),
            Achievement("consistent_7", "Week Streak", "Reach your goal for 7 days in a row", "consecutiveDays", 7, icon = "calendar", color = "#84cc16"),
            Achievement("consistent_14", "2 Week Streak", "Reach your goal for 14 days in a row", "consecutiveDays", 14, icon = "calendar-heart", color = "#22c55e"),
            Achievement("consistent_30", "Month Streak", "Reach your goal for 30 days in a row", "consecutiveDays", 30, icon = "calendar", color = "#22c55e"),
            Achievement("consistent_60", "2 Month Streak", "Reach your goal for 60 days in a row", "consecutiveDays", 60, icon = "calendar-star", color = "#10b981"),
            Achievement("consistent_100", "100 Day Streak", "Reach your goal for 100 days in a row", "consecutiveDays", 100, icon = "calendar-check", color = "#8b5cf6"),
            Achievement("consistent_365", "Year Streak", "Reach your goal for 365 days in a row", "consecutiveDays", 365, icon = "calendar-crown", color = "#ec4899"),
            
            // Distance
            Achievement("distance_1", "First KM", "Walk 1 km total", "totalDistance", 1, icon = "map-pin", color = "#22c55e"),
            Achievement("distance_10", "10 KM", "Walk 10 km total", "totalDistance", 10, icon = "route", color = "#3b82f6"),
            Achievement("distance_50", "50 KM", "Walk 50 km total", "totalDistance", 50, icon = "map", color = "#f59e0b"),
            Achievement("distance_100", "100 KM", "Walk 100 km total", "totalDistance", 100, icon = "route", color = "#6366f1"),
            Achievement("distance_200", "200 KM", "Walk 200 km total", "totalDistance", 200, icon = "compass", color = "#8b5cf6"),
            Achievement("distance_500", "500 KM", "Walk 500 km total", "totalDistance", 500, icon = "route", color = "#a855f7"),
            Achievement("distance_1000", "1,000 KM", "Walk 1,000 km total", "totalDistance", 1000, icon = "globe", color = "#ec4899"),
            Achievement("distance_5000", "5,000 KM", "Walk 5,000 km total", "totalDistance", 5000, icon = "earth", color = "#7c3aed"),
            
            // Calories
            Achievement("calories_1k", "1K Burn", "Burn 1,000 calories total", "caloriesBurned", 1000, icon = "flame", color = "#22c55e"),
            Achievement("calories_5k", "5K Burn", "Burn 5,000 calories total", "caloriesBurned", 5000, icon = "flame", color = "#3b82f6"),
            Achievement("calories_10k", "10K Calories", "Burn 10,000 calories total", "caloriesBurned", 10000, icon = "flame", color = "#f97316"),
            Achievement("calories_25k", "25K Calories", "Burn 25,000 calories total", "caloriesBurned", 25000, icon = "fire", color = "#f59e0b"),
            Achievement("calories_50k", "50K Calories", "Burn 50,000 calories total", "caloriesBurned", 50000, icon = "flame", color = "#ef4444"),
            Achievement("calories_100k", "100K Calories", "Burn 100,000 calories total", "caloriesBurned", 100000, icon = "fire", color = "#dc2626"),
            Achievement("calories_500k", "500K Calories", "Burn 500,000 calories total", "caloriesBurned", 500000, icon = "meteor", color = "#7c3aed"),
            
            // Active Time
            Achievement("active_30min", "30 Min Active", "Be active for 30 minutes in a day", "activeTime", 30, icon = "timer", color = "#22c55e"),
            Achievement("active_1hour", "1 Hour Active", "Be active for 1 hour in a day", "activeTime", 60, icon = "clock", color = "#3b82f6"),
            Achievement("active_2hours", "2 Hours Active", "Be active for 2 hours in a day", "activeTime", 120, icon = "hourglass", color = "#f59e0b"),
            Achievement("active_5hours", "5 Hours Active", "Be active for 5 hours total", "activeTime", 300, icon = "watch", color = "#8b5cf6"),
            Achievement("active_10hours", "10 Hours Active", "Be active for 10 hours total", "activeTime", 600, icon = "alarm-clock", color = "#ec4899"),
            Achievement("active_50hours", "50 Hours Active", "Be active for 50 hours total", "activeTime", 3000, icon = "stopwatch", color = "#7c3aed"),
            Achievement("active_100hours", "100 Hours Active", "Be active for 100 hours total", "activeTime", 6000, icon = "time", color = "#a855f7"),
            
            // Special Achievements
            Achievement("early_bird", "Early Bird", "Walk 1,000 steps before 8 AM", "special", 1000, icon = "sunrise", color = "#fbbf24"),
            Achievement("night_owl", "Night Owl", "Walk 1,000 steps after 8 PM", "special", 1000, icon = "moon", color = "#6366f1"),
            Achievement("weekend_warrior", "Weekend Warrior", "Walk 15,000 steps on a weekend day", "special", 15000, icon = "calendar-event", color = "#ec4899"),
            Achievement("perfect_week", "Perfect Week", "Reach your goal every day for a week", "special", 7, icon = "check-circle", color = "#22c55e"),
            Achievement("speed_demon", "Speed Demon", "Walk at 6 km/h or faster", "special", 6, icon = "lightning", color = "#f59e0b"),
            Achievement("explorer", "Explorer", "Record 10 different locations", "special", 10, icon = "map-marker", color = "#3b82f6"),
            Achievement("social_sharer", "Social Sharer", "Share your progress 5 times", "special", 5, icon = "share-2", color = "#8b5cf6"),
            Achievement("settings_master", "Settings Master", "Customize your profile", "special", 1, icon = "user-check", color = "#10b981")
        )
        achievementDao.insertAll(achievements)
    }

    private suspend fun checkAchievements() {
        val totalSteps = dailyDataDao.getTotalSteps() ?: 0
        val totalDistance = dailyDataDao.getTotalDistance()?.toInt() ?: 0
        val totalKcal = dailyDataDao.getTotalCalories() ?: 0
        
        val today = LocalDate.now()
        val todayData = dailyDataDao.getByDate(today)
        val todaySteps = todayData?.currentSteps ?: 0
        
        // Check daily steps achievements
        if (todaySteps >= 1000) achievementDao.unlock("first_steps", today, todaySteps)
        if (todaySteps >= 5000) achievementDao.unlock("goal_setter", today, todaySteps)
        if (todaySteps >= 10000) achievementDao.unlock("energizer", today, todaySteps)
        if (todaySteps >= 15000) achievementDao.unlock("fire_walker", today, todaySteps)
        if (todaySteps >= 20000) achievementDao.unlock("champion", today, todaySteps)
        if (todaySteps >= 30000) achievementDao.unlock("step_master", today, todaySteps)
        
        // Update progress for locked achievements
        achievementDao.updateProgress("first_steps", todaySteps)
        achievementDao.updateProgress("goal_setter", todaySteps)
        achievementDao.updateProgress("energizer", todaySteps)
        
        // Check distance achievements
        if (totalDistance >= 100) achievementDao.unlock("distance_100", today, totalDistance)
        if (totalDistance >= 500) achievementDao.unlock("distance_500", today, totalDistance)
        achievementDao.updateProgress("distance_100", totalDistance)
        achievementDao.updateProgress("distance_500", totalDistance)
        
        // Check calories achievements
        if (totalKcal >= 10000) achievementDao.unlock("calories_10k", today, totalKcal)
        if (totalKcal >= 50000) achievementDao.unlock("calories_50k", today, totalKcal)
        achievementDao.updateProgress("calories_10k", totalKcal)
        achievementDao.updateProgress("calories_50k", totalKcal)
    }

    // User Settings
    fun getUserSettings(): Flow<UserSettings?> = userSettingsDao.get()

    suspend fun updateSettings(settings: UserSettings) = withContext(Dispatchers.IO) {
        userSettingsDao.update(settings)
    }

    suspend fun getOrCreateSettings(): UserSettings = withContext(Dispatchers.IO) {
        userSettingsDao.getSync() ?: UserSettings().also {
            userSettingsDao.insert(it)
        }
    }
}

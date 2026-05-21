package com.steptracker.nativeapp

import android.app.Application
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StepTrackerApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with default data on first run
        applicationScope.launch {
            val repository = DataRepository(this@StepTrackerApplication)
            
            // Ensure user settings exist
            repository.getOrCreateSettings()
            
            // Initialize achievements if needed
            repository.initializeAchievements()
            
            // Create today's data entry
            repository.getOrCreateTodayData()
        }
    }
}

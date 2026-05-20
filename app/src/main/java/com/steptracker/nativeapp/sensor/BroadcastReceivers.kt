package com.steptracker.nativeapp.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Receiver to handle device boot - restore any saved state
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reset step counter on boot since TYPE_STEP_COUNTER resets
            val stepManager = StepCounterManager.getInstance(context)
            stepManager.resetDaily()
            
            // Schedule daily reset alarm
            DailyResetReceiver.scheduleDailyReset(context)
        }
    }
}

// Receiver for daily step reset at midnight
class DailyResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repo = DataRepository(context)
        val stepManager = StepCounterManager.getInstance(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            // Save yesterday's data
            val yesterday = LocalDate.now().minusDays(1)
            val steps = stepManager.currentSteps.value
            
            repo.updateSteps(yesterday, steps)
            
            // Reset for today
            stepManager.resetDaily()
            
            // Create new entry for today
            repo.getOrCreateTodayData()
            
            // Reschedule for next midnight
            scheduleDailyReset(context)
        }
    }
    
    companion object {
        fun scheduleDailyReset(context: Context) {
            // In a real app, use AlarmManager to schedule exact alarm
            // For simplicity, this is handled by WorkManager or system in production
        }
    }
}

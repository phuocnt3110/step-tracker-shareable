package com.steptracker.nativeapp.sensor

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.steptracker.nativeapp.data.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

// Receiver to handle device boot - restore any saved state
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed — scheduling daily reset")
            // Don't resetDaily here; StepCounterManager handles reboot detection
            // via sensor value going backwards. Just reschedule the alarm.
            DailyResetReceiver.scheduleDailyReset(context)
        }
    }
    
    companion object {
        private const val TAG = "BootReceiver"
    }
}

// Receiver for daily step reset at midnight
class DailyResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Midnight reset triggered")
        val repo = DataRepository(context)
        val stepManager = StepCounterManager.getInstance(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            // Reset for today (StepCounterManager.init already handles date check,
            // but this ensures reset if app is in background)
            stepManager.resetDaily()
            
            // Create new entry for today
            repo.getOrCreateTodayData()
            
            // Reschedule for next midnight
            scheduleDailyReset(context)
        }
    }
    
    companion object {
        private const val TAG = "DailyResetReceiver"
        private const val REQUEST_CODE = 10001

        fun scheduleDailyReset(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, DailyResetReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule for next midnight (00:00:05 to avoid edge cases)
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
                set(Calendar.MILLISECOND, 0)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    midnight.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    midnight.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Daily reset scheduled for ${midnight.time}")
        }
    }
}

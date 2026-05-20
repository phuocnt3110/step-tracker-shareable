package com.steptracker.nativeapp.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.ui.MainActivity
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityTrackingService : Service() {
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    lateinit var locationTracker: LocationTracker
        private set
    lateinit var stepCounterManager: StepCounterManager
        private set
    
    private var isTracking = false
    private var startTime: LocalDateTime? = null
    private var startSteps = 0
    
    var onStatsUpdate: ((ActivityStats) -> Unit)? = null
    
    data class ActivityStats(
        val duration: String,
        val steps: Int,
        val distance: Double,
        val calories: Int,
        val speed: Float
    )
    
    inner class LocalBinder : Binder() {
        fun getService(): ActivityTrackingService = this@ActivityTrackingService
    }
    
    override fun onCreate() {
        super.onCreate()
        locationTracker = LocationTracker(this)
        stepCounterManager = StepCounterManager.getInstance(this)
        
        locationTracker.setListener(object : LocationTracker.LocationListener {
            override fun onLocationUpdate(coordinate: LocationTracker.Coordinate, distanceDelta: Double, totalDistance: Double) {
                updateNotification()
            }
            
            override fun onSpeedUpdate(currentSpeed: Float, maxSpeed: Float) {
                // Update UI through callback
            }
        })
        
        stepCounterManager.onStepUpdate = { steps ->
            updateNotification()
        }
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    fun startTracking(): Boolean {
        if (isTracking) return true
        
        if (!locationTracker.startTracking()) {
            return false
        }
        
        stepCounterManager.startTracking()
        startSteps = stepCounterManager.currentSteps.value
        startTime = LocalDateTime.now()
        isTracking = true
        
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        startStatsUpdates()
        
        return true
    }
    
    fun stopTracking() {
        isTracking = false
        locationTracker.stopTracking()
        stepCounterManager.stopTracking()
        stopStatsUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    
    fun isTracking(): Boolean = isTracking
    
    fun getCurrentStats(): ActivityStats {
        val duration = startTime?.let { start ->
            val now = LocalDateTime.now()
            val seconds = java.time.Duration.between(start, now).seconds
            val minutes = seconds / 60
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            val remainingSeconds = seconds % 60
            
            when {
                hours > 0 -> String.format("%d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
                else -> String.format("%d:%02d", minutes, remainingSeconds)
            }
        } ?: "0:00"
        
        val currentSteps = stepCounterManager.currentSteps.value
        val steps = if (currentSteps >= startSteps) currentSteps - startSteps else currentSteps
        
        // Calculate calories: approximately 0.04 kcal per step
        val calories = (steps * 0.04).toInt()
        
        return ActivityStats(
            duration = duration,
            steps = steps,
            distance = locationTracker.totalDistance.value,
            calories = calories,
            speed = locationTracker.currentSpeed.value
        )
    }
    
    private fun startStatsUpdates() {
        serviceScope.launch {
            while (isActive && isTracking) {
                onStatsUpdate?.invoke(getCurrentStats())
                delay(1000) // Update every second
            }
        }
    }
    
    private fun stopStatsUpdates() {
        serviceScope.coroutineContext.cancelChildren()
    }
    
    private fun createNotification(): Notification {
        val channelId = "activity_tracking_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Activity Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current activity tracking progress"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "activity")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stats = getCurrentStats()
        
        return Notification.Builder(this, channelId)
            .apply {
                setContentTitle("Step Tracker - Recording Activity")
                setContentText("${stats.steps} steps · ${String.format("%.2f", stats.distance)} km · ${stats.calories} kcal")
                setSmallIcon(R.drawable.ic_notification)
                setContentIntent(pendingIntent)
                setOngoing(true)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                }
            }
            .build()
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (isTracking) {
            stopTracking()
        }
    }
    
    companion object {
        const val NOTIFICATION_ID = 1001
    }
}

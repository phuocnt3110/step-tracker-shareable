package com.steptracker.nativeapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StepCounterManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs: SharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
    
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    
    // Initial step count from sensor (for TYPE_STEP_COUNTER which is cumulative since boot)
    private var initialStepCount = 0
    private var lastRawSensorValue = 0
    private var hasInitialReading = false
    
    // For apps without step counter sensor, use detector
    private var detectedSteps = 0
    
    // Track which date the steps belong to
    private var savedDate: String = ""
    
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps
    
    // Flag: true once we have a valid sensor reading (not stale prefs)
    private val _hasSensorData = MutableStateFlow(false)
    val hasSensorData: StateFlow<Boolean> = _hasSensorData
    
    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable
    
    var onStepUpdate: ((Int) -> Unit)? = null
    
    init {
        // Try to get step counter sensor first (more accurate, cumulative)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        // Fallback to step detector (triggers once per step)
        if (stepCounterSensor == null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        }
        
        _isAvailable.value = stepCounterSensor != null || stepDetectorSensor != null
        
        // Load saved state
        savedDate = prefs.getString("saved_date", "") ?: ""
        initialStepCount = prefs.getInt("initial_step_count", 0)
        lastRawSensorValue = prefs.getInt("last_raw_sensor", 0)
        hasInitialReading = prefs.getBoolean("has_initial", false)
        detectedSteps = prefs.getInt("detected_steps", 0)
        
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        if (savedDate == todayStr) {
            // Same day — restore steps
            _currentSteps.value = prefs.getInt("current_steps", 0)
            Log.d(TAG, "Restored ${_currentSteps.value} steps for today ($todayStr)")
        } else {
            // New day — reset for today
            Log.d(TAG, "New day detected ($savedDate → $todayStr), resetting steps")
            detectedSteps = 0
            hasInitialReading = false
            _currentSteps.value = 0
            savedDate = todayStr
            saveState()
        }
    }
    
    fun startTracking() {
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    fun stopTracking() {
        sensorManager.unregisterListener(this)
        saveState()
    }
    
    fun resetDaily() {
        detectedSteps = 0
        hasInitialReading = false
        _currentSteps.value = 0
        _hasSensorData.value = false
        savedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        saveState()
    }
    
    /**
     * Restore steps from DB on startup (source of truth).
     * Called by MainActivity BEFORE startTracking() to avoid the 0→real jump.
     */
    fun restoreFromDb(dbSteps: Int) {
        if (!_hasSensorData.value && dbSteps > _currentSteps.value) {
            _currentSteps.value = dbSteps
            Log.d(TAG, "Restored $dbSteps steps from DB")
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        // Day changed while tracking — auto-reset
        if (savedDate != todayStr) {
            Log.d(TAG, "Day changed during tracking, resetting")
            detectedSteps = 0
            hasInitialReading = false
            _currentSteps.value = 0
            savedDate = todayStr
        }
        
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                // This sensor returns cumulative steps since device boot
                val rawSensorSteps = event.values[0].toInt()
                
                if (!hasInitialReading) {
                    // First reading: either fresh start or device rebooted
                    // If we have saved steps from DB/prefs, offset the sensor
                    // so that todaySteps starts from the saved value
                    val savedSteps = _currentSteps.value
                    initialStepCount = rawSensorSteps - savedSteps
                    hasInitialReading = true
                    Log.d(TAG, "Initial sensor=$rawSensorSteps, offset=$initialStepCount, restored=$savedSteps")
                } else if (rawSensorSteps < lastRawSensorValue) {
                    // Sensor value went backwards → device rebooted
                    val savedSteps = _currentSteps.value
                    initialStepCount = rawSensorSteps - savedSteps
                    Log.d(TAG, "Reboot detected: sensor=$rawSensorSteps < last=$lastRawSensorValue, re-offset")
                }
                
                lastRawSensorValue = rawSensorSteps
                val todaySteps = (rawSensorSteps - initialStepCount).coerceAtLeast(0)
                
                _currentSteps.value = todaySteps
                _hasSensorData.value = true
                onStepUpdate?.invoke(todaySteps)
                
                // Persist on every sensor event
                saveState()
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // This sensor triggers once per step (value is always 1.0)
                detectedSteps++
                _currentSteps.value = detectedSteps
                _hasSensorData.value = true
                onStepUpdate?.invoke(detectedSteps)
                
                // Save every 10 steps to reduce I/O
                if (detectedSteps % 10 == 0) {
                    saveState()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not needed
    }
    
    private fun saveState() {
        prefs.edit().apply {
            putString("saved_date", savedDate)
            putInt("initial_step_count", initialStepCount)
            putInt("last_raw_sensor", lastRawSensorValue)
            putBoolean("has_initial", hasInitialReading)
            putInt("detected_steps", detectedSteps)
            putInt("current_steps", _currentSteps.value)
            apply()
        }
    }
    
    companion object {
        private const val TAG = "StepCounterManager"
        
        @Volatile
        private var INSTANCE: StepCounterManager? = null
        
        fun getInstance(context: Context): StepCounterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StepCounterManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

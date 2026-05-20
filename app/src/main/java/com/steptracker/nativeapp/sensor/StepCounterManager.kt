package com.steptracker.nativeapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val prefs: SharedPreferences = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
    
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    
    // Initial step count from sensor (for TYPE_STEP_COUNTER which is cumulative)
    private var initialStepCount = 0
    private var hasInitialReading = false
    
    // For apps without step counter sensor, use detector
    private var detectedSteps = 0
    
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps
    
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
        
        // Load saved offset
        initialStepCount = prefs.getInt("initial_step_count", 0)
        hasInitialReading = prefs.getBoolean("has_initial", false)
        detectedSteps = prefs.getInt("detected_steps", 0)
        _currentSteps.value = prefs.getInt("current_steps", 0)
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
        saveState()
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                // This sensor returns cumulative steps since device boot
                val totalSteps = event.values[0].toInt()
                
                if (!hasInitialReading) {
                    initialStepCount = totalSteps
                    hasInitialReading = true
                    prefs.edit().apply {
                        putInt("initial_step_count", initialStepCount)
                        putBoolean("has_initial", true)
                        apply()
                    }
                }
                
                val todaySteps = totalSteps - initialStepCount
                if (todaySteps >= 0) {
                    _currentSteps.value = todaySteps
                    onStepUpdate?.invoke(todaySteps)
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // This sensor triggers once per step (value is always 1.0)
                detectedSteps++
                _currentSteps.value = detectedSteps
                onStepUpdate?.invoke(detectedSteps)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not needed
    }
    
    private fun saveState() {
        prefs.edit().apply {
            putInt("initial_step_count", initialStepCount)
            putBoolean("has_initial", hasInitialReading)
            putInt("detected_steps", detectedSteps)
            putInt("current_steps", _currentSteps.value)
            apply()
        }
    }
    
    companion object {
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

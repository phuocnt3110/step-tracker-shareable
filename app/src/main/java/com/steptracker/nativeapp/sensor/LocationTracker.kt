package com.steptracker.nativeapp.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class LocationTracker(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private var isTracking = false
    
    private val _coordinates = MutableStateFlow<List<Coordinate>>(emptyList())
    val coordinates: StateFlow<List<Coordinate>> = _coordinates
    
    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance
    
    private val _currentSpeed = MutableStateFlow(0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed
    
    private val _maxSpeed = MutableStateFlow(0f)
    val maxSpeed: StateFlow<Float> = _maxSpeed
    
    data class Coordinate(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float,
        val timestamp: Long,
        val speed: Float,
        val altitude: Double
    )
    
    interface LocationListener {
        fun onLocationUpdate(coordinate: Coordinate, distanceDelta: Double, totalDistance: Double)
        fun onSpeedUpdate(currentSpeed: Float, maxSpeed: Float)
    }
    
    private var listener: LocationListener? = null
    
    fun setListener(listener: LocationListener) {
        this.listener = listener
    }
    
    fun startTracking(): Boolean {
        if (isTracking) return true
        if (!hasLocationPermission()) return false
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(3)
        ).apply {
            setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(1))
            setMinUpdateDistanceMeters(5f)
        }.build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    processLocation(location)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            isTracking = true
            return true
        } catch (e: SecurityException) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun processLocation(location: Location) {
        // Filter out low accuracy locations
        if (location.accuracy > 20) return
        
        val coordinate = Coordinate(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            timestamp = location.time,
            speed = location.speed,
            altitude = location.altitude
        )
        
        val currentList = _coordinates.value
        var distanceDelta = 0.0
        
        if (currentList.isNotEmpty()) {
            val last = currentList.last()
            distanceDelta = calculateDistance(last.latitude, last.longitude, coordinate.latitude, coordinate.longitude)
        }
        
        _coordinates.value = currentList + coordinate
        _totalDistance.value += distanceDelta
        
        // Update speed
        val speedKmh = location.speed * 3.6f // Convert m/s to km/h
        _currentSpeed.value = speedKmh
        if (speedKmh > _maxSpeed.value) {
            _maxSpeed.value = speedKmh
        }
        
        listener?.onLocationUpdate(coordinate, distanceDelta, _totalDistance.value)
        listener?.onSpeedUpdate(_currentSpeed.value, _maxSpeed.value)
    }
    
    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        isTracking = false
    }
    
    fun reset() {
        _coordinates.value = emptyList()
        _totalDistance.value = 0.0
        _currentSpeed.value = 0f
        _maxSpeed.value = 0f
    }
    
    fun getCurrentLocation(onResult: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }
        
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                onResult(location)
            }.addOnFailureListener {
                onResult(null)
            }
        } catch (e: SecurityException) {
            onResult(null)
        }
    }
    
    fun isTracking(): Boolean = isTracking
    
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in km
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return R * c
    }
}

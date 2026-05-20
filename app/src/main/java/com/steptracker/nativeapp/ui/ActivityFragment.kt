package com.steptracker.nativeapp.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.DataRepository
import com.steptracker.nativeapp.sensor.ActivityTrackingService
import kotlinx.coroutines.launch

class ActivityFragment : Fragment() {
    private lateinit var viewModel: ActivityViewModel
    private lateinit var btnStartStop: Button
    private lateinit var tvDuration: TextView
    private lateinit var tvSteps: TextView
    private lateinit var tvKcal: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    
    private var trackingService: ActivityTrackingService? = null
    private var serviceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ActivityTrackingService.LocalBinder
            trackingService = binder.getService()
            serviceBound = true
            updateButtonState()
            
            trackingService?.onStatsUpdate = { stats ->
                activity?.runOnUiThread {
                    updateStats(stats)
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            serviceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = DataRepository(requireContext())
        viewModel = ViewModelProvider(this, ActivityViewModel.Factory(repo))[ActivityViewModel::class.java]
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_activity, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View All button
        view.findViewById<View>(R.id.btnViewAll)?.setOnClickListener {
            val intent = Intent(requireContext(), ActivityListActivity::class.java)
            startActivity(intent)
        }
        
        btnStartStop = view.findViewById(R.id.btnStartStop)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvSteps = view.findViewById(R.id.tvSteps)
        tvKcal = view.findViewById(R.id.tvKcal)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        
        btnStartStop.setOnClickListener {
            toggleTracking()
        }
        
        // Bind to service
        Intent(requireContext(), ActivityTrackingService::class.java).also { intent ->
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        
        observeData()
    }
    
    private fun toggleTracking() {
        val service = trackingService
        if (service == null) {
            Toast.makeText(requireContext(), "Service not ready", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (service.isTracking()) {
            // Stop tracking
            val stats = service.getCurrentStats()
            service.stopTracking()
            
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.saveActivity(
                    steps = stats.steps,
                    kcal = stats.calories,
                    km = stats.distance,
                    durationMinutes = parseDuration(stats.duration),
                    maxSpeed = service.locationTracker.maxSpeed.value.toDouble(),
                    avgSpeed = stats.speed.toDouble(),
                    coordinates = service.locationTracker.coordinates.value.map { 
                        Pair(it.latitude, it.longitude) 
                    }
                )
            }
            
            updateButtonState()
            clearStats()
        } else {
            // Start tracking
            if (service.startTracking()) {
                updateButtonState()
            } else {
                Toast.makeText(requireContext(), "Cannot start tracking. Check permissions.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun parseDuration(duration: String): Int {
        // Parse "12:34" or "1:23:45" format
        val parts = duration.split(":").map { it.toIntOrNull() ?: 0 }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1] // MM:SS
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2] // HH:MM:SS
            else -> 0
        }
    }
    
    private fun updateButtonState() {
        val isTracking = trackingService?.isTracking() ?: false
        btnStartStop.text = if (isTracking) "Stop Activity" else "Start Activity"
        btnStartStop.setBackgroundColor(
            if (isTracking) 
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.red_500)
            else
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.emerald_500)
        )
    }
    
    private fun updateStats(stats: ActivityTrackingService.ActivityStats) {
        tvDuration.text = stats.duration
        tvSteps.text = stats.steps.toString()
        tvKcal.text = stats.calories.toString()
        tvDistance.text = String.format("%.2f", stats.distance)
        tvSpeed.text = String.format("%.1f", stats.speed)
    }
    
    private fun clearStats() {
        tvDuration.text = "0:00"
        tvSteps.text = "0"
        tvKcal.text = "0"
        tvDistance.text = "0.00"
        tvSpeed.text = "0.0"
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentActivities.collect { activities ->
                    // Update recent activities list
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        if (serviceBound) {
            requireContext().unbindService(serviceConnection)
        }
    }
}

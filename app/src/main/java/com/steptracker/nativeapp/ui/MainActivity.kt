package com.steptracker.nativeapp.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.DataRepository
import com.steptracker.nativeapp.sensor.ActivityTrackingService
import com.steptracker.nativeapp.sensor.StepCounterManager
import com.nphlab.sdk.ads.NphAds
import com.nphlab.sdk.ads.listener.NphAdListener
import com.nphlab.sdk.ads.AdError
import androidx.activity.OnBackPressedCallback
import com.steptracker.nativeapp.util.LanguageUtil
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val langCode = LanguageUtil.getSavedLanguage(newBase)
        super.attachBaseContext(LanguageUtil.applyLanguage(newBase, langCode))
    }

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var repository: DataRepository
    private lateinit var stepCounterManager: StepCounterManager
    
    private var trackingService: ActivityTrackingService? = null
    private var serviceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ActivityTrackingService.LocalBinder
            trackingService = binder.getService()
            serviceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            serviceBound = false
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            initializeApp()
        } else {
            Toast.makeText(this, "Permissions required for step tracking", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        repository = DataRepository(this)
        stepCounterManager = StepCounterManager.getInstance(this)
        
        setupBottomNavigation()
        
        // Schedule daily midnight reset
        com.steptracker.nativeapp.sensor.DailyResetReceiver.scheduleDailyReset(this)
        
        // Preload ads for sub-screens
        NphAds.preload(this, AdNamespaces.INTER_MAIN)
        NphAds.preload(this, AdNamespaces.INTER_SETTINGS)
        NphAds.preload(this, AdNamespaces.INTER_ACTIVITY_DETAIL)
        NphAds.preload(this, AdNamespaces.INTER_ACHIEVEMENT_BACK)
        NphAds.preload(this, AdNamespaces.NATIVE_ACTIVITY_LIST)
        NphAds.preload(this, AdNamespaces.NATIVE_REPORT)
        
        // Handle back button: go to home tab or minimize app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If not on home tab → switch to home tab
                if (bottomNav.selectedItemId != R.id.nav_steps) {
                    bottomNav.selectedItemId = R.id.nav_steps
                } else {
                    // Already on home → minimize app instead of closing
                    moveTaskToBack(true)
                }
            }
        })
        
        if (checkPermissions()) {
            initializeApp()
        } else {
            requestPermissions()
        }
        
        // Bind to tracking service
        Intent(this, ActivityTrackingService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    private var currentTabId = R.id.nav_steps

    private fun setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNavigation)
        
        bottomNav.setOnItemSelectedListener { item ->
            val previousTab = currentTabId
            currentTabId = item.itemId
            when (item.itemId) {
                R.id.nav_steps -> {
                    // Show interstitial when leaving Achievement tab
                    if (previousTab == R.id.nav_achievement) {
                        showInterstitialAndThen(AdNamespaces.INTER_ACHIEVEMENT_BACK) {
                            showFragment(StepsFragment())
                        }
                    } else {
                        showFragment(StepsFragment())
                    }
                    true
                }
                R.id.nav_activity -> {
                    if (previousTab == R.id.nav_achievement) {
                        showInterstitialAndThen(AdNamespaces.INTER_ACHIEVEMENT_BACK) {
                            showFragment(ActivityFragment())
                        }
                    } else {
                        showInterstitialAndThen(AdNamespaces.INTER_MAIN) {
                            showFragment(ActivityFragment())
                        }
                    }
                    true
                }
                R.id.nav_report -> {
                    if (previousTab == R.id.nav_achievement) {
                        showInterstitialAndThen(AdNamespaces.INTER_ACHIEVEMENT_BACK) {
                            showFragment(ReportFragment())
                        }
                    } else {
                        showFragment(ReportFragment())
                    }
                    true
                }
                R.id.nav_achievement -> {
                    showFragment(AchievementFragment())
                    true
                }
                else -> false
            }
        }
        
        // Show initial fragment
        if (supportFragmentManager.fragments.isEmpty()) {
            showFragment(StepsFragment())
        }
    }
    
    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showInterstitialAndThen(nameSpace: String, onComplete: () -> Unit) {
        // Prevent resume ad from firing right after interstitial dismisses
        NphAds.pauseResumeAds()
        var completed = false
        val safeComplete = {
            if (!completed) {
                completed = true
                // Re-enable resume ads after a brief delay (avoid immediate trigger)
                bottomNav.postDelayed({ NphAds.resumeResumeAds() }, 2000)
                onComplete()
            }
        }
        NphAds.showInterstitial(
            activity = this,
            nameSpace = nameSpace,
            listener = object : NphAdListener() {
                override fun onAdDismissed() {
                    safeComplete()
                }
                override fun onAdFailed(error: AdError) {
                    safeComplete()
                }
            }
        )
    }
    
    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        permissionLauncher.launch(permissions.toTypedArray())
    }
    
    private fun initializeApp() {
        lifecycleScope.launch {
            // Initialize achievements on first run
            repository.initializeAchievements()
            
            // Ensure today's data exists and restore step count from DB
            val todayData = repository.getOrCreateTodayData()
            stepCounterManager.restoreFromDb(todayData.currentSteps)
            
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Check and start step counter
                if (stepCounterManager.isAvailable.value) {
                    stepCounterManager.startTracking()
                    
                    // Only write to DB when we have real sensor data (not the initial 0)
                    stepCounterManager.currentSteps.collect { steps ->
                        if (stepCounterManager.hasSensorData.value || steps > 0) {
                            repository.updateSteps(java.time.LocalDate.now(), steps)
                        }
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity, 
                        "No step sensor available on this device", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    fun getTrackingService(): ActivityTrackingService? = trackingService
    
    override fun onStop() {
        super.onStop()
        // Persist steps to DB when app goes to background
        val steps = stepCounterManager.currentSteps.value
        if (steps > 0) {
            lifecycleScope.launch {
                repository.updateSteps(java.time.LocalDate.now(), steps)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stepCounterManager.stopTracking()
        NphAds.destroy(this)
        if (serviceBound) {
            unbindService(serviceConnection)
        }
    }
}

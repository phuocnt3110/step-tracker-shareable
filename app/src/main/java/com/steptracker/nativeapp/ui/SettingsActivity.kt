package com.steptracker.nativeapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nphlab.sdk.ads.NphAds
import com.nphlab.sdk.ads.listener.NphAdListener
import com.nphlab.sdk.ads.AdError
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.data.DataRepository
import com.steptracker.nativeapp.data.UserSettings
import com.steptracker.nativeapp.ui.language.LanguageActivity
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var repository: DataRepository
    
    // Profile views
    private lateinit var ivAvatar: ShapeableImageView
    private lateinit var btnEditAvatar: ImageButton
    private lateinit var etUserName: EditText
    private lateinit var etDailyGoal: EditText
    private lateinit var etWeight: EditText
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var btnSave: MaterialButton
    
    // App info views
    private lateinit var btnLanguage: LinearLayout
    private lateinit var btnShare: LinearLayout
    private lateinit var btnRate: LinearLayout
    private lateinit var btnAbout: LinearLayout
    private lateinit var btnPrivacy: LinearLayout
    
    private var currentSettings: UserSettings? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        repository = DataRepository(this)
        
        initViews()
        setupListeners()
        loadSettings()
    }
    
    private fun initViews() {
        // Profile
        ivAvatar = findViewById(R.id.ivAvatar)
        btnEditAvatar = findViewById(R.id.btnEditAvatar)
        etUserName = findViewById(R.id.etUserName)
        etDailyGoal = findViewById(R.id.etDailyGoal)
        etWeight = findViewById(R.id.etWeight)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnSave = findViewById(R.id.btnSave)
        
        // App Info
        btnLanguage = findViewById(R.id.btnLanguage)
        btnShare = findViewById(R.id.btnShare)
        btnRate = findViewById(R.id.btnRate)
        btnAbout = findViewById(R.id.btnAbout)
        btnPrivacy = findViewById(R.id.btnPrivacy)
        
        // Toolbar
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        
        // Register back press callback with interstitial ad and timeout fallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            private var isHandling = false
            override fun handleOnBackPressed() {
                if (isHandling) return
                isHandling = true
                val handler = android.os.Handler(mainLooper)
                val fallback = Runnable { if (!isFinishing) finish() }
                handler.postDelayed(fallback, 3000)
                NphAds.showInterstitial(
                    activity = this@SettingsActivity,
                    nameSpace = AdNamespaces.INTER_SETTINGS,
                    listener = object : NphAdListener() {
                        override fun onAdDismissed() {
                            handler.removeCallbacks(fallback)
                            finish()
                        }
                        override fun onAdFailed(error: AdError) {
                            handler.removeCallbacks(fallback)
                            finish()
                        }
                    }
                )
            }
        })
    }
    
    private fun setupListeners() {
        btnSave.setOnClickListener { saveSettings() }
        
        btnEditAvatar.setOnClickListener {
            Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show()
        }
        
        // App info actions
        btnLanguage.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            intent.putExtra("from_settings", true)
            startActivity(intent)
        }
        btnShare.setOnClickListener { shareApp() }
        btnRate.setOnClickListener { rateApp() }
        btnAbout.setOnClickListener { showAbout() }
        btnPrivacy.setOnClickListener { showPrivacyPolicy() }
    }
    
    private fun loadSettings() {
        lifecycleScope.launch {
            currentSettings = repository.getOrCreateSettings()
            currentSettings?.let { settings ->
                etUserName.setText(settings.userName)
                etDailyGoal.setText(settings.dailyGoal.toString())
                etWeight.setText(settings.weight.toString())
                switchNotifications.isChecked = settings.notificationsEnabled
                
            }
        }
    }
    
    private fun saveSettings() {
        val userName = etUserName.text.toString().trim()
        val dailyGoal = etDailyGoal.text.toString().toIntOrNull() ?: 8000
        val weight = etWeight.text.toString().toDoubleOrNull() ?: 70.0
        val notifications = switchNotifications.isChecked
        val settings = UserSettings(
            userName = userName,
            dailyGoal = dailyGoal,
            weight = weight,
            notificationsEnabled = notifications,
            language = "English"
        )
        
        lifecycleScope.launch {
            repository.updateSettings(settings)
            Toast.makeText(this@SettingsActivity, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
    
    private fun showAbout() {
        Toast.makeText(this, getString(R.string.about_message), Toast.LENGTH_LONG).show()
    }
    
    private fun showPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
        startActivity(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        NphAds.destroy(this)
        super.onDestroy()
    }
}

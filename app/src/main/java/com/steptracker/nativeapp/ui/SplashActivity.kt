package com.steptracker.nativeapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.ui.language.LanguageActivity
import com.steptracker.nativeapp.util.LanguageUtil
import com.nphlab.sdk.ads.NphAds

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler = android.os.Handler(mainLooper)
        val navigateRunnable = Runnable { navigateNext() }

        // Wait 1.5s for SDK to fully initialize, then show splash
        handler.postDelayed({
            try {
                NphAds.showSplash(this) {
                    // Ad finished or failed — navigate immediately
                    handler.removeCallbacks(navigateRunnable)
                    navigateNext()
                }
            } catch (e: Exception) {
                navigateNext()
            }
        }, 1500)

        // Safety timeout: max 8 seconds on splash screen
        handler.postDelayed(navigateRunnable, 8000)
    }

    private fun navigateNext() {
        val intent = if (LanguageUtil.isLanguageSelected(this)) {
            // Language already chosen — apply and go to Main
            LanguageUtil.updateResource(this, LanguageUtil.getSavedLanguage(this))
            Intent(this, MainActivity::class.java)
        } else {
            // First launch — show language picker
            Intent(this, LanguageActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

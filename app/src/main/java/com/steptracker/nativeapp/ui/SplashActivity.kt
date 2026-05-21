package com.steptracker.nativeapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.ui.language.LanguageActivity
import com.steptracker.nativeapp.util.LanguageUtil

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 1500)
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

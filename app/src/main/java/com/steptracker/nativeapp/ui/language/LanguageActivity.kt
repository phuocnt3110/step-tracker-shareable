package com.steptracker.nativeapp.ui.language

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steptracker.nativeapp.R
import com.steptracker.nativeapp.ui.MainActivity
import com.steptracker.nativeapp.util.LanguageUtil
import java.util.Locale

class LanguageActivity : AppCompatActivity() {

    private lateinit var adapter: LanguageAdapter
    private lateinit var ivBack: ImageView
    private lateinit var ivDone: ImageView
    private lateinit var rvLanguages: RecyclerView

    private var selectedCode = ""
    private var initialCode = ""
    private var isFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        isFromSettings = intent.getBooleanExtra("from_settings", false)

        ivBack = findViewById(R.id.iv_back)
        ivDone = findViewById(R.id.iv_done)
        rvLanguages = findViewById(R.id.rv_all_languages)

        setupUI()
        setupRecyclerView()
    }

    private fun setupUI() {
        ivBack.visibility = if (isFromSettings) View.VISIBLE else View.GONE
        ivDone.visibility = View.GONE

        ivBack.setOnClickListener { finish() }

        ivDone.setOnClickListener {
            saveAndNavigate()
        }
    }

    private fun setupRecyclerView() {
        val savedCode = LanguageUtil.getSavedLanguage(this)
        val systemLangCode = Locale.getDefault().language

        initialCode = savedCode.ifEmpty { systemLangCode }
        selectedCode = initialCode

        adapter = LanguageAdapter { onLanguageSelected(it) }
        rvLanguages.layoutManager = LinearLayoutManager(this)
        rvLanguages.adapter = adapter

        val languages = getLanguages()
        adapter.submitList(languages.map { it.copy(isSelected = it.code == selectedCode) })
    }

    private fun getLanguages(): List<LanguageItem> {
        return listOf(
            LanguageItem("pt", "Português", "\uD83C\uDDE7\uD83C\uDDF7"),
            LanguageItem("hi", "हिंदी", "\uD83C\uDDEE\uD83C\uDDF3"),
            LanguageItem("fr", "Français", "\uD83C\uDDEB\uD83C\uDDF7"),
            LanguageItem("es", "Español", "\uD83C\uDDEA\uD83C\uDDF8"),
            LanguageItem("en", "English", "\uD83C\uDDFA\uD83C\uDDF8"),
            LanguageItem("ar", "العربية", "\uD83C\uDDF8\uD83C\uDDE6"),
            LanguageItem("bg", "Български", "\uD83C\uDDE7\uD83C\uDDEC"),
            LanguageItem("cs", "Čeština", "\uD83C\uDDE8\uD83C\uDDFF"),
            LanguageItem("da", "Dansk", "\uD83C\uDDE9\uD83C\uDDF0"),
            LanguageItem("de", "Deutsch", "\uD83C\uDDE9\uD83C\uDDEA"),
            LanguageItem("el", "Ελληνικά", "\uD83C\uDDEC\uD83C\uDDF7"),
            LanguageItem("fa", "فارسی", "\uD83C\uDDEE\uD83C\uDDF7"),
            LanguageItem("vi", "Tiếng Việt", "\uD83C\uDDFB\uD83C\uDDF3")
        )
    }

    private fun onLanguageSelected(item: LanguageItem) {
        if (selectedCode == item.code) return
        selectedCode = item.code
        adapter.setSelected(item.code)
        ivDone.visibility = if (selectedCode != initialCode) View.VISIBLE else View.GONE
    }

    private fun saveAndNavigate() {
        LanguageUtil.saveLanguage(this, selectedCode)
        LanguageUtil.updateResource(this, selectedCode)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

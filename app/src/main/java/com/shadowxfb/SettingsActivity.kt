package com.shadowxfb

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.shadowxfb.utils.PreferencesHelper

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        editor = prefs.edit()

        setupToolbar()
        setupThemeMode()
        setupLanguage()
        setupOfflineMode()
        setupOfflineLimits()
        setupBackgroundPlayback()
        setupAdBlocking()
        setupHideOptions()
        setupMessengerMode()
        setupClearData()
        setupAboutSection()
        setupDeveloperInfo()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupThemeMode() {
        val layout = findViewById<LinearLayout>(R.id.layoutTheme)
        val textTheme = findViewById<TextView>(R.id.textThemeValue)
        
        val currentTheme = PreferencesHelper.getThemeMode(this)
        textTheme.text = when (currentTheme) {
            "dark" -> getString(R.string.dark)
            "light" -> getString(R.string.light)
            else -> getString(R.string.system_default)
        }

        layout.setOnClickListener {
            val options = arrayOf(
                getString(R.string.system_default),
                getString(R.string.light),
                getString(R.string.dark)
            )
            
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.select_theme))
                .setItems(options) { _, which ->
                    val themeMode = when (which) {
                        0 -> "system"
                        1 -> "light"
                        2 -> "dark"
                        else -> "system"
                    }
                    PreferencesHelper.setThemeMode(this, themeMode)
                    textTheme.text = options[which]
                    
                    when (themeMode) {
                        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    
                    recreate()
                }
                .show()
        }
    }

    private fun setupLanguage() {
        val layout = findViewById<LinearLayout>(R.id.layoutLanguage)
        val textLang = findViewById<TextView>(R.id.textLanguageValue)
        
        val currentLang = PreferencesHelper.getLanguage(this)
        textLang.text = when (currentLang) {
            "bn" -> "বাংলা"
            "hi" -> "हिन्दी"
            else -> "English"
        }

        layout.setOnClickListener {
            val options = arrayOf("English", "বাংলা", "हिन्दी")
            val langCodes = arrayOf("en", "bn", "hi")
            
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(options) { _, which ->
                    val langCode = langCodes[which]
                    PreferencesHelper.setLanguage(this, langCode)
                    textLang.text = options[which]
                    
                    LanguageManager.setLocale(this, langCode)
                    recreate()
                }
                .show()
        }
    }

    private fun setupOfflineMode() {
        val switchOffline = findViewById<SwitchMaterial>(R.id.switchOfflineMode)
        switchOffline.isChecked = PreferencesHelper.isOfflineModeEnabled(this)
        switchOffline.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setOfflineMode(this, isChecked)
            Toast.makeText(this, 
                if (isChecked) getString(R.string.offline_on) else getString(R.string.offline_off),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupOfflineLimits() {
        // Reel Limit
        val seekReel = findViewById<SeekBar>(R.id.seekReelLimit)
        val textReel = findViewById<TextView>(R.id.textReelLimit)
        val currentReelLimit = PreferencesHelper.getReelLimit(this)
        seekReel.progress = currentReelLimit
        textReel.text = getString(R.string.reels_limit, currentReelLimit)
        
        seekReel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val limit = when {
                    progress < 10 -> 10
                    progress > 300 -> 300
                    else -> progress
                }
                textReel.text = getString(R.string.reels_limit, limit)
                PreferencesHelper.setReelLimit(this@SettingsActivity, limit)
                OfflineManager.updateReelLimit(limit)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // News Limit
        val seekNews = findViewById<SeekBar>(R.id.seekNewsLimit)
        val textNews = findViewById<TextView>(R.id.textNewsLimit)
        val currentNewsLimit = PreferencesHelper.getNewsLimit(this)
        seekNews.progress = currentNewsLimit
        textNews.text = getString(R.string.news_limit, currentNewsLimit)
        
        seekNews.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val limit = when {
                    progress < 10 -> 10
                    progress > 300 -> 300
                    else -> progress
                }
                textNews.text = getString(R.string.news_limit, limit)
                PreferencesHelper.setNewsLimit(this@SettingsActivity, limit)
                OfflineManager.updateNewsLimit(limit)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupBackgroundPlayback() {
        val switchBg = findViewById<SwitchMaterial>(R.id.switchBgPlayback)
        switchBg.isChecked = PreferencesHelper.isBackgroundPlaybackEnabled(this)
        switchBg.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setBackgroundPlayback(this, isChecked)
            Toast.makeText(this,
                if (isChecked) getString(R.string.bg_playback_on) else getString(R.string.bg_playback_off),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdBlocking() {
        val switchAd = findViewById<SwitchMaterial>(R.id.switchAdBlock)
        switchAd.isChecked = PreferencesHelper.isAdBlockingEnabled(this)
        switchAd.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setAdBlocking(this, isChecked)
            AdBlocker.setEnabled(isChecked)
            Toast.makeText(this,
                if (isChecked) getString(R.string.ad_block_on) else getString(R.string.ad_block_off),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupHideOptions() {
        val hideOptions = arrayOf(
            getString(R.string.marketplace),
            getString(R.string.groups),
            getString(R.string.pages),
            getString(R.string.notifications),
            getString(R.string.events),
            getString(R.string.watch),
            getString(R.string.saved)
        )

        val listView = findViewById<ListView>(R.id.listHideOptions)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, hideOptions)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        val savedHidden = PreferencesHelper.getHiddenOptions(this)
        for (i in hideOptions.indices) {
            if (savedHidden.contains(hideOptions[i])) {
                listView.setItemChecked(i, true)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = listView.checkedItemPositions
            val hiddenSet = mutableSetOf<String>()
            for (i in 0 until hideOptions.size) {
                if (selected.get(i)) {
                    hiddenSet.add(hideOptions[i])
                }
            }
            PreferencesHelper.setHiddenOptions(this, hiddenSet)
        }
    }

    private fun setupMessengerMode() {
        val switchMessenger = findViewById<SwitchMaterial>(R.id.switchMessengerMode)
        switchMessenger.isChecked = PreferencesHelper.useMessengerMode(this)
        switchMessenger.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setMessengerMode(this, isChecked)
            Toast.makeText(this,
                if (isChecked) "Messenger Mode ON" else "Messenger Mode OFF",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClearData() {
        val layout = findViewById<LinearLayout>(R.id.layoutClearData)
        layout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.clear_offline_title))
                .setMessage(getString(R.string.clear_offline_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    OfflineManager.clearAllData(this)
                    Toast.makeText(this, getString(R.string.offline_cleared), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
    }

    private fun setupAboutSection() {
        val layout = findViewById<LinearLayout>(R.id.layoutAbout)
        layout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("SHADOWX-FB")
                .setMessage("""
                    Version 2.0.0
                    
                    Ultimate Facebook Client
                    
                    Features:
                    • Ad Blocking
                    • Offline Mode (300 Reels, 300 News)
                    • Dark/Light Theme
                    • Multi Language Support
                    • Messenger Integration
                    • Background Playback
                    • Download Manager
                    • Customizable UI
                    
                    Made with ❤️
                """.trimIndent())
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }

    private fun setupDeveloperInfo() {
        val layout = findViewById<LinearLayout>(R.id.layoutDeveloper)
        layout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Developer")
                .setMessage("""
                    👨‍💻 Mueid Mursalin Rifat
                    
                    📱 Contact: https://www.facebook.com/mueid.mursalin.rifat1
                    
                    💡 Open Source Project
                """.trimIndent())
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
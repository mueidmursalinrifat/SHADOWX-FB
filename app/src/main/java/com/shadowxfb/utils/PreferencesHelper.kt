package com.shadowxfb.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferencesHelper {
    
    private fun getPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
    
    // Theme
    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString("theme_mode", "system") ?: "system"
    }
    
    fun setThemeMode(context: Context, mode: String) {
        getPrefs(context).edit().putString("theme_mode", mode).apply()
    }
    
    // Language
    fun getLanguage(context: Context): String {
        return getPrefs(context).getString("app_language", "en") ?: "en"
    }
    
    fun setLanguage(context: Context, language: String) {
        getPrefs(context).edit().putString("app_language", language).apply()
    }
    
    // Offline Mode
    fun isOfflineModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("offline_mode", true)
    }
    
    fun setOfflineMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("offline_mode", enabled).apply()
    }
    
    // Reel Limit
    fun getReelLimit(context: Context): Int {
        return getPrefs(context).getInt("reel_limit", 300)
    }
    
    fun setReelLimit(context: Context, limit: Int) {
        getPrefs(context).edit().putInt("reel_limit", limit).apply()
    }
    
    // News Limit
    fun getNewsLimit(context: Context): Int {
        return getPrefs(context).getInt("news_limit", 300)
    }
    
    fun setNewsLimit(context: Context, limit: Int) {
        getPrefs(context).edit().putInt("news_limit", limit).apply()
    }
    
    // Background Playback
    fun isBackgroundPlaybackEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("background_playback", true)
    }
    
    fun setBackgroundPlayback(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("background_playback", enabled).apply()
    }
    
    // Ad Blocking
    fun isAdBlockingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean("ad_blocking", true)
    }
    
    fun setAdBlocking(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("ad_blocking", enabled).apply()
    }
    
    // Hidden Options
    fun getHiddenOptions(context: Context): Set<String> {
        return getPrefs(context).getStringSet("hidden_options", setOf()) ?: setOf()
    }
    
    fun setHiddenOptions(context: Context, options: Set<String>) {
        getPrefs(context).edit().putStringSet("hidden_options", options).apply()
    }
    
    // Messenger Mode
    fun useMessengerMode(context: Context): Boolean {
        return getPrefs(context).getBoolean("messenger_mode", false)
    }
    
    fun setMessengerMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("messenger_mode", enabled).apply()
    }
}
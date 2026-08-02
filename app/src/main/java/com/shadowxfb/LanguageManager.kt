package com.shadowxfb

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LanguageManager {
    
    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", "en") ?: "en"
        return Locale(langCode)
    }
    
    fun applyLanguage(context: Context): Context {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", "en") ?: "en"
        
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }
}
package com.shadowxfb

import android.content.Context
import java.util.Locale

object LanguageManager {
    
    fun setLocale(context: Context, languageCode: String) {
        // Only English supported now
        val locale = Locale("en")
        Locale.setDefault(locale)
        
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getLocale(context: Context): Locale {
        return Locale("en")
    }
    
    fun applyLanguage(context: Context): Context {
        return context
    }
    
    fun getCurrentLanguage(context: Context): String {
        return "en"
    }
}

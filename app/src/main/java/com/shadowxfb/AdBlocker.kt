package com.shadowxfb

object AdBlocker {
    private var enabled = true
    
    private val adPatterns = listOf(
        "doubleclick.net",
        "googleadservices",
        "googleads",
        "facebook.com/ads",
        "facebook.com/sponsored",
        "sponsored",
        "fbads",
        "pagead",
        "adservice",
        "adserver",
        "ad.doubleclick",
        "adsense",
        "advertising",
        "advert",
        "promoted",
        "trk=ad",
        "ref=ad"
    )

    fun isAd(url: String?): Boolean {
        if (!enabled || url == null) return false
        
        val urlLower = url.lowercase()
        return adPatterns.any { urlLower.contains(it.lowercase()) }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun isEnabled(): Boolean = enabled
}
package com.shadowxfb

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shadowxfb.utils.NetworkUtils
import com.shadowxfb.utils.PreferencesHelper

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var offlineManager: OfflineManager
    private lateinit var downloadManager: DownloadManager
    private var isOfflineMode = false
    private var isDarkMode = false

    companion object {
        private const val FACEBOOK_URL = "https://m.facebook.com"
        private const val MESSENGER_URL = "https://www.messenger.com"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        offlineManager = OfflineManager(this)
        downloadManager = DownloadManager(this)

        setupWebView()
        checkPermissions()
        checkInternetAndLoad()
        registerNetworkCallback()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val data = intent.data
            if (data?.scheme == "shadowxfb") {
                val path = data.path
                if (path?.startsWith("/download/") == true) {
                    val url = path.removePrefix("/download/")
                    downloadManager.downloadFile(url)
                }
            }
        }
    }

    private fun applyTheme() {
        val themeMode = PreferencesHelper.getThemeMode(this)
        when (themeMode) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        isDarkMode = themeMode == "dark" || 
            (themeMode == "system" && (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_open_messenger -> {
                webView.loadUrl(MESSENGER_URL)
                true
            }
            R.id.action_refresh -> {
                webView.reload()
                Toast.makeText(this, getString(R.string.refreshing), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_download -> {
                val url = webView.url ?: ""
                if (url.isNotEmpty()) {
                    downloadManager.downloadFile(url)
                } else {
                    Toast.makeText(this, getString(R.string.no_url_to_download), Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_clear_cache -> {
                webView.clearCache(true)
                WebStorage.getInstance().deleteAllData()
                Toast.makeText(this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_clear_offline -> {
                showClearOfflineDialog()
                true
            }
            else -> false
        }
    }

    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                databaseEnabled = true
                loadsImagesAutomatically = true
                loadWithOverviewMode = true
                useWideViewPort = true
                
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false
                    
                    if (AdBlocker.isAd(url)) {
                        Toast.makeText(this@MainActivity, "🚫 " + getString(R.string.ad_blocked), Toast.LENGTH_SHORT).show()
                        return true
                    }
                    
                    if (url.startsWith("shadowxfb://download/")) {
                        val downloadUrl = url.removePrefix("shadowxfb://download/")
                        downloadManager.downloadFile(downloadUrl)
                        return true
                    }
                    
                    if (url.contains("messenger.com") || url.contains("m.me")) {
                        view?.loadUrl(url)
                        return true
                    }
                    
                    if (isOfflineMode && !url.startsWith("file://") && !url.startsWith("about:blank")) {
                        Toast.makeText(this@MainActivity, getString(R.string.offline_mode_active), Toast.LENGTH_SHORT).show()
                        return true
                    }
                    
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    if (isDarkMode) {
                        injectDarkMode()
                    }
                    
                    injectFeatures()
                    injectAdBlocker()
                    hideUnwantedElements()
                    
                    if (url?.contains(FACEBOOK_URL) == true || url?.contains(MESSENGER_URL) == true) {
                        offlineManager.startBackgroundCaching(view)
                    }
                }

                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    if (PreferencesHelper.isOfflineModeEnabled(this@MainActivity)) {
                        loadOfflineContent()
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    // Fullscreen video support
                }
                
                override fun onHideCustomView() {
                    // Exit fullscreen
                }
            }
        }
    }

    private fun injectDarkMode() {
        val css = """
            javascript:(function() {
                var style = document.createElement('style');
                style.innerHTML = `
                    * { 
                        background-color: #1a1a1a !important; 
                        color: #e0e0e0 !important; 
                    }
                    body, div, header, footer, section, article, main, aside, nav {
                        background-color: #1a1a1a !important;
                        color: #e0e0e0 !important;
                    }
                    a, p, span, h1, h2, h3, h4, h5, h6 {
                        color: #e0e0e0 !important;
                    }
                    input, textarea, select, button {
                        background-color: #2d2d2d !important;
                        color: #e0e0e0 !important;
                        border-color: #404040 !important;
                    }
                    ::-webkit-scrollbar {
                        width: 8px;
                        height: 8px;
                    }
                    ::-webkit-scrollbar-track {
                        background: #1a1a1a;
                    }
                    ::-webkit-scrollbar-thumb {
                        background: #404040;
                        border-radius: 4px;
                    }
                    ::-webkit-scrollbar-thumb:hover {
                        background: #555555;
                    }
                `;
                document.head.appendChild(style);
            })();
        """.trimIndent()
        webView.loadUrl(css)
    }

    private fun injectFeatures() {
        val script = """
            javascript:(function() {
                var links = document.querySelectorAll('a[href*=".apk"], a[href*=".zip"], a[href*=".pdf"]');
                links.forEach(function(link) {
                    link.onclick = function(e) {
                        e.preventDefault();
                        window.location.href = 'shadowxfb://download/' + link.href;
                    };
                });
            })();
        """.trimIndent()
        webView.loadUrl(script)
    }

    private fun injectAdBlocker() {
        val script = """
            javascript:(function() {
                var adSelectors = [
                    '.ads', '.ad', '.advertisement', '.sponsored',
                    '[class*="ad"]', '[id*="ad"]', '[class*="sponsored"]'
                ];
                adSelectors.forEach(function(selector) {
                    var elements = document.querySelectorAll(selector);
                    elements.forEach(function(el) {
                        el.style.display = 'none';
                    });
                });
            })();
        """.trimIndent()
        webView.loadUrl(script)
    }

    private fun hideUnwantedElements() {
        val hiddenOptions = PreferencesHelper.getHiddenOptions(this)
        if (hiddenOptions.isNotEmpty()) {
            val script = """
                javascript:(function() {
                    var options = ${hiddenOptions.joinToString(",") { "'$it'" }};
                    options.forEach(function(option) {
                        var elements = document.querySelectorAll('a[href*="' + option.toLowerCase() + '"], .' + option.toLowerCase());
                        elements.forEach(function(el) {
                            el.style.display = 'none';
                        });
                    });
                })();
            """.trimIndent()
            webView.loadUrl(script)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            val needPermissions = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(needPermissions.toTypedArray())
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }
    }

    private fun checkInternetAndLoad() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            isOfflineMode = false
            loadFacebookOnline()
        } else {
            if (PreferencesHelper.isOfflineModeEnabled(this)) {
                isOfflineMode = true
                loadOfflineContent()
            } else {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show()
                loadFacebookOnline()
            }
        }
    }

    private fun loadFacebookOnline() {
        val useMessenger = PreferencesHelper.useMessengerMode(this)
        val url = if (useMessenger) MESSENGER_URL else FACEBOOK_URL
        webView.loadUrl(url)
        Toast.makeText(this, getString(R.string.loading_facebook), Toast.LENGTH_SHORT).show()
    }

    private fun loadOfflineContent() {
        if (offlineManager.hasOfflineContent()) {
            webView.loadUrl("file://${offlineManager.getOfflineIndex()}")
            Toast.makeText(this, getString(R.string.offline_mode_activated), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.no_offline_content), Toast.LENGTH_SHORT).show()
            loadFacebookOnline()
        }
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        if (isOfflineMode && PreferencesHelper.isOfflineModeEnabled(this@MainActivity)) {
                            isOfflineMode = false
                            loadFacebookOnline()
                            Toast.makeText(this@MainActivity, getString(R.string.internet_connected), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onLost(network: Network) {
                    runOnUiThread {
                        if (PreferencesHelper.isOfflineModeEnabled(this@MainActivity)) {
                            isOfflineMode = true
                            loadOfflineContent()
                            Toast.makeText(this@MainActivity, getString(R.string.offline_mode_activated), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    private fun showClearOfflineDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.clear_offline_title))
            .setMessage(getString(R.string.clear_offline_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                offlineManager.clearAllOfflineData()
                Toast.makeText(this, getString(R.string.offline_cleared), Toast.LENGTH_SHORT).show()
                if (isOfflineMode) loadOfflineContent()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (PreferencesHelper.isBackgroundPlaybackEnabled(this)) {
            webView.onPause()
            webView.resumeTimers()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        if (PreferencesHelper.isOfflineModeEnabled(this) && !NetworkUtils.isNetworkAvailable(this)) {
            isOfflineMode = true
            loadOfflineContent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
        WebStorage.getInstance().deleteAllData()
    }
}

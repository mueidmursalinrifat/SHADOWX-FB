package com.shadowxfb

import android.app.DownloadManager as SystemDownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.net.toUri

class DownloadManager(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as SystemDownloadManager

    fun downloadFile(url: String) {
        try {
            val fileName = URLUtil.guessFileName(url, null, null)
            val request = SystemDownloadManager.Request(url.toUri()).apply {
                setTitle(fileName)
                setDescription("Downloading from SHADOWX-FB")
                setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                setAllowedNetworkTypes(
                    SystemDownloadManager.Request.NETWORK_WIFI or 
                    SystemDownloadManager.Request.NETWORK_MOBILE
                )
                setMimeType(guessMimeType(url))
            }
            
            downloadManager.enqueue(request)
            Toast.makeText(context, "📥 Download started: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun guessMimeType(url: String): String {
        return when {
            url.endsWith(".apk") -> "application/vnd.android.package-archive"
            url.endsWith(".zip") -> "application/zip"
            url.endsWith(".pdf") -> "application/pdf"
            url.endsWith(".mp4") -> "video/mp4"
            url.endsWith(".mp3") -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
}
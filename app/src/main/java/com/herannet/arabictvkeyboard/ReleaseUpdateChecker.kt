package com.herannet.arabictvkeyboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ReleaseUpdateChecker(private val context: Context) {
    data class Release(val tag: String, val pageUrl: String, val apkUrl: String?)

    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    fun check(callback: (Result<Release>) -> Unit) {
        executor.execute {
            val result = runCatching {
                val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "ArabicTVKeyboard/${BuildConfig.VERSION_NAME}")
                }
                connection.inputStream.bufferedReader().use { reader ->
                    if (connection.responseCode !in 200..299) error("GitHub HTTP ${connection.responseCode}")
                    val json = JSONObject(reader.readText())
                    val assets = json.optJSONArray("assets")
                    var apkUrl: String? = null
                    if (assets != null) for (index in 0 until assets.length()) {
                        val asset = assets.getJSONObject(index)
                        if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                            break
                        }
                    }
                    Release(
                        tag = json.optString("tag_name").ifBlank { error("Release has no tag") },
                        pageUrl = json.optString("html_url").ifBlank { error("Release has no page URL") },
                        apkUrl = apkUrl
                    )
                }
            }
            main.post { callback(result) }
        }
    }

    fun openRelease(release: Release) {
        val target = release.apkUrl ?: release.pageUrl
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    companion object {
        private const val API_URL = "https://api.github.com/repos/Hussamobeed/ArabicTVKeyboard/releases/latest"
    }
}

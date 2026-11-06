package com.example.recipes.data.importing

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

fun interface HtmlDownloader {
    /** Returns the page HTML. Throws [IOException] when the page cannot be downloaded. */
    suspend fun download(url: String): String
}

class OkHttpHtmlDownloader @Inject constructor(
    private val httpClient: OkHttpClient,
) : HtmlDownloader {

    override suspend fun download(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            // Some recipe sites refuse requests that do not look like a browser.
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml")
            .header("Accept-Language", "it-IT,it;q=0.9,en;q=0.5")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} for $url")
            response.body.string()
        }
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36"
    }
}

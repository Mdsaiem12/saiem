package com.saiem.sportsapp.data.remote.scraper

import com.saiem.sportsapp.data.model.StreamSource
import com.saiem.sportsapp.data.model.SourceType
import com.saiem.sportsapp.data.model.StreamQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject

/**
 * StreamScraper – scans multiple public stream aggregator sites to find
 * working HLS/MP4 streams for live football and cricket matches.
 *
 * Sources attempted (in priority order):
 * 1. streamedsu (direct API)
 * 2. sportsurge (scrape)
 * 3. ronaldo7 (scrape)
 * 4. livetv.sx (scrape)
 * 5. cricfree (scrape)
 */
class StreamScraper @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    companion object {
        private val SCRAPE_SOURCES = listOf(
            "https://streamed.su/api/stream",
            "https://sportsurge.net",
            "https://www.ronaldo7.net",
            "https://livetv.sx/enx",
            "https://cricfree.sc"
        )
        private val STREAM_PATTERNS = listOf(
            Regex("""(https?://[^\s"']+\.m3u8[^\s"']*)"""),
            Regex("""(https?://[^\s"']+/playlist[^\s"']*)"""),
            Regex("""source\s*:\s*["'](https?://[^"']+)["']"""),
            Regex("""file\s*:\s*["'](https?://[^"']+)["']"""),
            Regex("""src\s*=\s*["'](https?://[^"']+\.m3u8[^"']*)["']""")
        )
        private const val DEFAULT_UA =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
    }

    /** Scrape all known sources and return deduplicated stream URLs */
    suspend fun scrapeStreamsForMatch(matchKeyword: String): List<StreamSource> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<StreamSource>()
            var priority = 100

            for (source in SCRAPE_SOURCES) {
                try {
                    val streams = scrapeSource(source, matchKeyword, priority)
                    results.addAll(streams)
                    priority -= 10
                } catch (e: Exception) {
                    // silently skip failed sources
                }
            }

            results.distinctBy { it.streamUrl }
        }

    private fun scrapeSource(
        baseUrl: String,
        keyword: String,
        priority: Int
    ): List<StreamSource> {
        val request = Request.Builder()
            .url(baseUrl)
            .header("User-Agent", DEFAULT_UA)
            .header("Referer", baseUrl)
            .build()

        val body = okHttpClient.newCall(request).execute().use { it.body?.string() ?: "" }

        val streams = mutableListOf<StreamSource>()
        for (pattern in STREAM_PATTERNS) {
            pattern.findAll(body).forEach { matchResult ->
                val url = matchResult.groupValues[1]
                if (url.isNotBlank() && isValidStreamUrl(url)) {
                    streams.add(
                        StreamSource(
                            id = url.hashCode().toString(),
                            matchId = keyword,
                            title = "Stream from ${extractDomain(baseUrl)}",
                            streamUrl = url,
                            quality = detectQuality(url),
                            sourceType = detectSourceType(url),
                            referer = baseUrl,
                            priority = priority
                        )
                    )
                }
            }
        }
        return streams
    }

    private fun isValidStreamUrl(url: String): Boolean =
        url.contains(".m3u8") || url.contains(".mp4") ||
                url.contains("/stream") || url.contains("/live") ||
                url.contains("playlist")

    private fun detectQuality(url: String): StreamQuality = when {
        url.contains("hd", ignoreCase = true) || url.contains("720") || url.contains("1080") ->
            StreamQuality.HD
        url.contains("sd", ignoreCase = true) || url.contains("480") || url.contains("360") ->
            StreamQuality.SD
        else -> StreamQuality.AUTO
    }

    private fun detectSourceType(url: String): SourceType = when {
        url.endsWith(".m3u8") || url.contains(".m3u8?") -> SourceType.M3U8
        url.endsWith(".mp4") || url.contains(".mp4?") -> SourceType.MP4
        url.contains(".mpd") -> SourceType.DASH
        else -> SourceType.M3U8
    }

    private fun extractDomain(url: String): String =
        runCatching { java.net.URL(url).host }.getOrDefault(url)
}

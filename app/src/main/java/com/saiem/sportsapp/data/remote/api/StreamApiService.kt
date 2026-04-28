package com.saiem.sportsapp.data.remote.api

import com.saiem.sportsapp.data.model.StreamSource
import com.saiem.sportsapp.data.model.TvChannel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ── Stream API (Firebase + fallback public APIs) ───────────────────────────────
interface StreamApiService {

    // Sportzfy-compatible stream endpoint
    @GET("streams/match/{matchId}")
    suspend fun getMatchStreams(
        @Path("matchId") matchId: String
    ): Response<StreamListResponse>

    // TV Channels list
    @GET("channels")
    suspend fun getTvChannels(
        @Query("category") category: String = "sports"
    ): Response<ChannelListResponse>

    // Validate a single stream URL
    @GET("streams/check")
    suspend fun checkStream(
        @Query("url") streamUrl: String
    ): Response<StreamCheckResponse>
}

data class StreamListResponse(
    val streams: List<StreamData> = emptyList(),
    val matchId: String = ""
)

data class StreamData(
    val id: String = "",
    val url: String = "",
    val quality: String = "HD",
    val language: String = "EN",
    val title: String = "",
    val referer: String = "",
    val priority: Int = 0
)

data class ChannelListResponse(
    val channels: List<ChannelData> = emptyList()
)

data class ChannelData(
    val id: String = "",
    val name: String = "",
    val logo: String = "",
    val streamUrl: String = "",
    val category: String = "SPORTS",
    val country: String = ""
)

data class StreamCheckResponse(
    val isLive: Boolean = false,
    val url: String = ""
)

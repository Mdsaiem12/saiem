package com.saiem.sportsapp.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages rotating API keys from Firebase RemoteConfig.
 * Falls back to hardcoded placeholders if RemoteConfig is unavailable.
 */
@Singleton
class ApiKeyManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    // Replace placeholder values with your real API keys in Firebase RemoteConfig
    fun getFootballApiKey(): String =
        remoteConfig.getString("football_data_api_key").ifBlank { "YOUR_FOOTBALL_DATA_API_KEY" }

    fun getCricketApiKey(): String =
        remoteConfig.getString("cricket_api_key").ifBlank { "YOUR_CRICKET_API_KEY" }

    fun getSportsMonksKey(): String =
        remoteConfig.getString("sportmonks_api_key").ifBlank { "YOUR_SPORTMONKS_KEY" }
}

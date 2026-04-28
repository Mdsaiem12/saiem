package com.saiem.sportsapp.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.saiem.sportsapp.data.local.SportsDatabase
import com.saiem.sportsapp.data.local.dao.ChannelDao
import com.saiem.sportsapp.data.local.dao.MatchDao
import com.saiem.sportsapp.data.remote.api.CricketApiService
import com.saiem.sportsapp.data.remote.api.FootballDataApiService
import com.saiem.sportsapp.data.remote.api.StreamApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .header("User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
                .build()
            chain.proceed(req)
        }
        .build()

    @Provides
    @Singleton
    @Named("football")
    fun provideFootballRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.football-data.org/v4/")
        .client(
            okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .header("X-Auth-Token", "YOUR_FOOTBALL_DATA_API_KEY")
                        .build()
                    chain.proceed(req)
                }
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("cricket")
    fun provideCricketRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.cricapi.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("stream")
    fun provideStreamRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://your-backend.firebaseapp.com/api/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideFootballApiService(@Named("football") retrofit: Retrofit): FootballDataApiService =
        retrofit.create(FootballDataApiService::class.java)

    @Provides
    @Singleton
    fun provideCricketApiService(@Named("cricket") retrofit: Retrofit): CricketApiService =
        retrofit.create(CricketApiService::class.java)

    @Provides
    @Singleton
    fun provideStreamApiService(@Named("stream") retrofit: Retrofit): StreamApiService =
        retrofit.create(StreamApiService::class.java)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRemoteConfig(): FirebaseRemoteConfig =
        FirebaseRemoteConfig.getInstance().also { rc ->
            rc.setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            })
            rc.setDefaultsAsync(
                mapOf(
                    "football_data_api_key" to "",
                    "cricket_api_key" to "",
                    "sportmonks_api_key" to ""
                )
            )
            rc.fetchAndActivate()
        }

    @Provides
    @Singleton
    fun provideSportsDatabase(@ApplicationContext ctx: Context): SportsDatabase =
        Room.databaseBuilder(ctx, SportsDatabase::class.java, "sports_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMatchDao(db: SportsDatabase): MatchDao = db.matchDao()

    @Provides
    fun provideChannelDao(db: SportsDatabase): ChannelDao = db.channelDao()
}

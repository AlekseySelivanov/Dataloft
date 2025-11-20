package com.example.dataloft.di

import com.example.dataloft.BuildConfig
import com.example.dataloft.data.remote.TimerApi
import com.example.dataloft.data.repository.TimerRepositoryImpl
import com.example.dataloft.domain.repository.TimerRepository
import com.example.dataloft.workout.FakeTrackGenerator
import com.example.dataloft.workout.WorkoutAudioCuePlayer
import com.example.dataloft.workout.WorkoutSessionManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

object ServiceLocator {
    private const val BASE_URL = "https://sr111.05.testing.place/api/v2/"
    private const val APP_TOKEN = "secret"
    private const val AUTH_TOKEN = "Bearer pdhO16atBIXogpPzaLDjDcl5Gpmbz9Mdl1mjhrhWZBuOgNCgxDlk7mMIbFcEc7mj"

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val newRequest = chain.request()
            .newBuilder()
            .addHeader("App-Token", APP_TOKEN)
            .addHeader("Authorization", AUTH_TOKEN)
            .build()
        chain.proceed(newRequest)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val timerApi: TimerApi by lazy {
        retrofit.create()
    }

    val timerRepository: TimerRepository by lazy {
        TimerRepositoryImpl(timerApi)
    }

    val workoutSessionManager: WorkoutSessionManager by lazy {
        WorkoutSessionManager(
            trackGenerator = FakeTrackGenerator(),
            audioCuePlayer = WorkoutAudioCuePlayer()
        )
    }
}

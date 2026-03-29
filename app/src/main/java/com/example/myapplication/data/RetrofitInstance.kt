package com.example.myapplication.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://wger.de/api/v2/"
    private const val NINJA_BASE_URL = "https://api.api-ninjas.com/v1/"
    private const val HF_BASE_URL = "https://api-inference.huggingface.co/"
    private const val EXERCISE_DB_BASE_URL = "https://exercisedb.p.rapidapi.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    val exerciseDbApi: ExerciseDbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EXERCISE_DB_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ExerciseDbApiService::class.java)
    }

    val ninjaApi: NinjaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NINJA_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NinjaApiService::class.java)
    }

    val huggingFaceApi: HuggingFaceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(HF_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(HuggingFaceApiService::class.java)
    }
}

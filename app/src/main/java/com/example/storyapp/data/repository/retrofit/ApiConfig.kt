package com.example.storyapp.data.repository.retrofit

import android.content.Context
import com.example.storyapp.data.repository.UserPreference
import com.example.storyapp.data.repository.dataStore
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {

    companion object {
        fun getApiService(context: Context): ApiService {
            val userPreference = UserPreference.getInstance(context.dataStore)

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val token = runBlocking { userPreference.getToken() }
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer $token")
                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://story-api.dicoding.dev/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}

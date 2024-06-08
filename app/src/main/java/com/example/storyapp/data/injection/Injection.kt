package com.example.storyapp.data.injection

import android.content.Context
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.repository.UserPreference
import com.example.storyapp.data.repository.dataStore
import com.example.storyapp.data.repository.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(context)
        return UserRepository.getInstance(pref, apiService)
    }
}
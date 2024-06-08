package com.example.storyapp.maps

import android.content.Context
import retrofit2.Callback
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.repository.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.repository.response.LocatedListStoryItem
import com.example.storyapp.data.repository.response.LocatedStoryResponse
import com.example.storyapp.data.repository.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class MapsViewModel(private val repository: UserRepository): ViewModel() {

    private val _listStoryResponse = MutableLiveData<List<LocatedListStoryItem?>?>()
    val listStoryResponse: LiveData<List<LocatedListStoryItem?>?> = _listStoryResponse


    private val _userSession = MutableLiveData<UserModel>()
    val userSession: LiveData<UserModel> = _userSession

    init {
        // Load the initial session from the repository
        viewModelScope.launch {
            repository.getSession().collect { session ->
                _userSession.postValue(session)
            }
        }
    }

    fun getStoriesWithLocation(token: String, context : Context) {
        Log.i("Token", token)
        ApiConfig.getApiService(context).getLocatedStory("Bearer $token", 1).enqueue(object : Callback<LocatedStoryResponse> {
            override fun onResponse(call: Call<LocatedStoryResponse>, response: Response<LocatedStoryResponse>) {
                if (response.isSuccessful) {
                    if (response.body()?.error == false){
                        _listStoryResponse.postValue(response.body()?.listStory)
                    }
                } else {
                    Log.e("ErrorResponse", "Response code: ${response.code()}, message: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LocatedStoryResponse>, t: Throwable) {
                Log.e("Failed", "Failed to get Located Story Response", t)
            }
        })
    }
}


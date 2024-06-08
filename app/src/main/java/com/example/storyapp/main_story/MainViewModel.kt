package com.example.storyapp.main_story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.repository.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.repository.response.ListStoryItem
import com.example.storyapp.data.repository.response.StoriesResponse
import com.example.storyapp.data.repository.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    val storyResponse: LiveData<PagingData<ListStoryItem>> = repository.getStories().cachedIn(viewModelScope)

    private val _userSession = MutableLiveData<UserModel>()
    val userSession: LiveData<UserModel> get() = _userSession

    init {
        // Load the initial session from the repository
        viewModelScope.launch {
            repository.getSession().collect { session ->
                _userSession.postValue(session)
            }
        }
    }

    fun logout(){
        viewModelScope.launch {
            repository.logout()
        }
    }

}
package com.example.storyapp.main_story.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.response.DetailStoryResponse
import com.example.storyapp.data.repository.response.Story
import com.example.storyapp.data.repository.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel : ViewModel() {

    private val _detailResponse = MutableLiveData<Story>()
    val detailResponse : LiveData<Story> get() = _detailResponse

    fun setDetail(id : String, token : String, context : Context){
        Log.i("DetailId", id)
        Log.i("DetailToken", token)
        ApiConfig.getApiService(context).getDetailtories("Bearer ${token}", id).enqueue(object :
            Callback<DetailStoryResponse> {
            override fun onResponse(
                call: Call<DetailStoryResponse>,
                response: Response<DetailStoryResponse>
            ) {
                if (response.body()?.error == false){
                    _detailResponse.postValue(response.body()?.story!!)
                } else {
                    Log.e("DetailFailed", "Failed to Get Detail Story")
                }
            }

            override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                Log.e("DetailStoryRequestFailed", "Request Get Detail Story Failed")
            }

        })
    }

    fun getDetail(): LiveData<Story> {
        return detailResponse
    }
}
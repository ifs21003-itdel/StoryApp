package com.example.storyapp.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.SingleLiveEvent
import com.example.storyapp.data.repository.response.RegisterResponse
import com.example.storyapp.data.repository.retrofit.ApiConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _registResponse = SingleLiveEvent<RegisterResponse?>()
    val registResponse: LiveData<RegisterResponse?> get() = _registResponse

    fun register(name: String, email: String, password: String, context : Context) {
        ApiConfig.getApiService(context).postRegister(name, email, password).enqueue(object :
            Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    _registResponse.postValue(response.body())
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorResponse = convertErrorBody(errorBody)
                        _registResponse.postValue(errorResponse)
                    } ?: run {
                        _registResponse.postValue(RegisterResponse(error = true, message = "Register failed: Unknown error"))
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _registResponse.postValue(RegisterResponse(error = true, message = "Register request failed: ${t.message}"))
            }
        })
    }

    private fun convertErrorBody(errorBody: ResponseBody): RegisterResponse? {
        return try {
            val type = object : TypeToken<RegisterResponse>() {}.type
            Gson().fromJson(errorBody.charStream(), type)
        } catch (e: Exception) {
            RegisterResponse(error = true, message = "Register failed: Unable to parse error response")
        }
    }
}

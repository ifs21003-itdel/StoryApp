    package com.example.storyapp.auth

    import android.content.Context
    import android.util.Log
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.storyapp.SingleLiveEvent
    import com.example.storyapp.data.repository.UserRepository
    import com.example.storyapp.data.repository.UserModel
    import com.example.storyapp.data.repository.response.LoginResponse
    import com.example.storyapp.data.repository.retrofit.ApiConfig
    import com.google.gson.Gson
    import com.google.gson.reflect.TypeToken
    import kotlinx.coroutines.launch
    import okhttp3.ResponseBody
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response

    class AuthenticationViewModel(private val repository: UserRepository) : ViewModel() {

        private val _loginResponse = SingleLiveEvent<LoginResponse?>()
        val loginResponse: LiveData<LoginResponse?> get() = _loginResponse

        private val _userSession = SingleLiveEvent<UserModel>()
        val userSession: LiveData<UserModel> get() = _userSession

        init {
            // Load the initial session from the repository
            viewModelScope.launch {
                repository.getSession().collect { session ->
                    _userSession.postValue(session)
                }
            }
        }

        fun saveSession(user: UserModel) {
            viewModelScope.launch {
                repository.saveSession(user)
                _userSession.postValue(user)
            }
        }

        fun checkAccount(email: String, password: String, context : Context) {
            ApiConfig.getApiService(context).postlogin(email, password).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        _loginResponse.postValue(response.body())
                        if (response.body()?.error == false) {
                            val user = response.body()?.loginResult?.token?.let { UserModel(email, it, true) }
                            if (user != null) {
                                saveSession(user)
                            }
                        } else {
                            response.errorBody()?.let { errorBody ->
                                val errorResponse = convertErrorBody(errorBody)
                                _loginResponse.postValue(errorResponse)
                            } ?: run {
                                _loginResponse.postValue(LoginResponse(error = true, message = "Login failed: Unknown error"))
                            }
                        }
                    } else {
                        response.errorBody()?.let { errorBody ->
                            val errorResponse = convertErrorBody(errorBody)
                            _loginResponse.postValue(errorResponse)
                        } ?: run {
                            _loginResponse.postValue(LoginResponse(error = true, message = "Login failed: Unknown error"))
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    _loginResponse.postValue(LoginResponse(error = true, message = "Login request failed: ${t.message}"))
                    Log.e("AuthenticationViewModel", "Login request failed", t)
                }
            })
        }

        private fun convertErrorBody(errorBody: ResponseBody): LoginResponse? {
            return try {
                val type = object : TypeToken<LoginResponse>() {}.type
                Gson().fromJson(errorBody.charStream(), type)
            } catch (e: Exception) {
                LoginResponse(error = true, message = "Login failed: Unable to parse error response")
            }
        }
    }


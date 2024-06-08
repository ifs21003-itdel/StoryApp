package com.example.storyapp.data.repository.retrofit

import com.example.storyapp.data.repository.response.AddStoryResponse
import com.example.storyapp.data.repository.response.DetailStoryResponse
import com.example.storyapp.data.repository.response.LocatedStoryResponse
import com.example.storyapp.data.repository.response.LoginResponse
import com.example.storyapp.data.repository.response.RegisterResponse
import com.example.storyapp.data.repository.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun postlogin(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun postRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @Multipart
    @POST("stories")
    fun postAddStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<AddStoryResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String
    ): Call<StoriesResponse>

    @GET("stories/{id}")
    fun getDetailtories(
        @Header("Authorization") token: String,
        @Path ("id") id : String
    ): Call<DetailStoryResponse>

    @GET("stories")
    fun getLocatedStory(
        @Header("Authorization") authorization: String,
        @Query("location") location : Int
    ): Call<LocatedStoryResponse>

    @GET("stories")
    suspend fun getStories(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ) : StoriesResponse
}
package com.example.storyapp.data.repository

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)

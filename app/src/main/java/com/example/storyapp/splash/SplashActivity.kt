package com.example.storyapp.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.storyapp.main_story.MainActivity
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.auth.AuthenticationActivity
import com.example.storyapp.auth.AuthenticationViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val viewModel by viewModels<AuthenticationViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Observe the userSession LiveData
        viewModel.userSession.observe(this, Observer { userSession ->
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = if (userSession.isLogin) {
                    Log.e("LoggedIn", "Logged In")
                    Log.i("Token", userSession.token)
                    Intent(this, MainActivity::class.java)
                } else {
                    Log.e("NotLoggedIn", "Not Logged In")
                    Intent(this, AuthenticationActivity::class.java)
                }
                startActivity(intent)
                finish()
            }, 1000)
        })
    }
}
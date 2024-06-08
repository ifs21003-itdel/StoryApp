package com.example.storyapp.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.storyapp.main_story.MainActivity
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel by viewModels<AuthenticationViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playAnimation()

        binding.toRegister.setOnClickListener {
            val intent = Intent(this@AuthenticationActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (validEmail() == null && binding.edLoginPassword.validatePassword() == null) {
                showLoading(true)
                viewModel.checkAccount(email, password, this)
            } else {
                makeToast("Invalid email or password")
            }
        }

        binding.edLoginPassword.setHelperTextView(binding.passwordHelperText)

        binding.edLoginEmail.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailLayout.helperText = validEmail()
            }
        }

        viewModel.loginResponse.observe(this, Observer {
            showLoading(false)
            it?.let {
                if (it.error == false) {
                    makeToast("Login Successful")
                    val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    makeToast("Login Failed, ${it.message}")
                }
            }
        })
    }

    private fun validEmail(): String? {
        val email = binding.edLoginEmail.text.toString()
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid Email"
        } else {
            null
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressBar3.visibility = View.VISIBLE
            binding.loadingOverlay.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            binding.progressBar3.visibility = View.GONE
            binding.loadingOverlay.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun playAnimation() {
        val welcome = ObjectAnimator.ofFloat(binding.welcome, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val applogo = ObjectAnimator.ofFloat(binding.applogo, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val together = AnimatorSet().apply {
            playTogether(welcome, applogo)
        }
        together.start()

        binding.emailLayout.alpha = 0f
        binding.passwordLayout.alpha = 0f
        binding.login.alpha = 0f
        binding.textView3.alpha = 0f
        binding.toRegister.alpha = 0f

        val email = ObjectAnimator.ofFloat(binding.emailLayout, View.ALPHA, 0f, 1f).setDuration(300)
        val password = ObjectAnimator.ofFloat(binding.passwordLayout, View.ALPHA, 0f, 1f).setDuration(300)
        val loginBtn = ObjectAnimator.ofFloat(binding.login, View.ALPHA, 0f, 1f).setDuration(300)
        val registText = ObjectAnimator.ofFloat(binding.textView3, View.ALPHA, 0f, 1f).setDuration(300)
        val registBtn = ObjectAnimator.ofFloat(binding.toRegister, View.ALPHA, 0f, 1f).setDuration(300)

        val togetherRegist = AnimatorSet().apply {
            playTogether(registText, registBtn)
        }

        AnimatorSet().apply {
            playSequentially(email, password, loginBtn, togetherRegist)
            start()
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


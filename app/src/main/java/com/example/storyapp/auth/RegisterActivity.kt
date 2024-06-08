package com.example.storyapp.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playAnimation()

        binding.toLogin.setOnClickListener {
            intent = Intent(this@RegisterActivity, AuthenticationActivity::class.java)
            startActivity(intent)
        }

        binding.register.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (validEmail() == null && validEmail() == null && binding.edRegisterPassword.validatePassword() == null){
                viewModel.register(name, email, password, this)
            }
        }

        viewModel.registResponse.observe(this, Observer { response ->
            if (response != null) {
                if (response.error == false) {
                    makeToast("Register Successful")
                    intent = Intent(this@RegisterActivity, AuthenticationActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    makeToast("Register Failed, ${response.message}")
                }
            }
        })

        binding.edRegisterName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.nameLayout.helperText = validName()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.nameLayout.helperText = validName()
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        })

        binding.edRegisterEmail.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailLayout.helperText = validEmail()
            }
        }

        binding.edRegisterPassword.setHelperTextView(binding.passwordHelperText)
    }

    private fun validName(): String {
        val name = binding.edRegisterName.text.toString()
        return if (name.isNotBlank()) "" else "required"
    }

    private fun validEmail(): String? {
        val email = binding.edRegisterEmail.text.toString()
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else "Invalid Email"
    }

    private fun playAnimation() {
        binding.textView2.alpha = 0f
        binding.nameLayout.alpha = 0f
        binding.emailLayout.alpha = 0f
        binding.passwordLayout.alpha = 0f
        binding.register.alpha = 0f
        binding.textView.alpha = 0f
        binding.toLogin.alpha = 0f

        val title = ObjectAnimator.ofFloat(binding.textView2, View.ALPHA, 0f, 1f).setDuration(250)
        val name = ObjectAnimator.ofFloat(binding.nameLayout, View.ALPHA, 0f, 1f).setDuration(250)
        val email = ObjectAnimator.ofFloat(binding.emailLayout, View.ALPHA, 0f, 1f).setDuration(250)
        val password = ObjectAnimator.ofFloat(binding.passwordLayout, View.ALPHA, 0f, 1f).setDuration(250)
        val register = ObjectAnimator.ofFloat(binding.register, View.ALPHA, 0f, 1f).setDuration(250)
        val loginText = ObjectAnimator.ofFloat(binding.textView, View.ALPHA, 0f, 1f).setDuration(250)
        val loginBtn = ObjectAnimator.ofFloat(binding.toLogin, View.ALPHA, 0f, 1f).setDuration(250)

        val togetherLogin = AnimatorSet().apply {
            playTogether(loginText, loginBtn)
        }

        AnimatorSet().apply {
            playSequentially(title, name, email, password, register, togetherLogin)
            start()
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

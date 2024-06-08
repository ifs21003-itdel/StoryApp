package com.example.storyapp.main_story.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailBinding
    private val viewModel by viewModels<DetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    companion object{
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TOKEN = "extra_token"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var id = intent.getStringExtra(EXTRA_ID)
        var token = intent.getStringExtra(EXTRA_TOKEN)
        if (id != null && token != null) {
            showDetail(id, token)
        }
    }

    fun showDetail(id : String, token : String){
        viewModel.setDetail(id, token, this)
        viewModel.getDetail().observe(this, Observer {
            Glide.with(this).load(it.photoUrl).centerCrop().into(binding.imageView)
            binding.title.text = it.name.toString()
            binding.textDescription.text = it.description.toString()
        })
    }
}
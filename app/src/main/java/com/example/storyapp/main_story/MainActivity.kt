package com.example.storyapp.main_story

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.add_story.AddStoryActivity
import com.example.storyapp.add_story.AddStoryViewModel
import com.example.storyapp.auth.AuthenticationActivity
import com.example.storyapp.data.repository.response.ListStoryItem
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.main_story.adapter.StoryAdapter
import com.example.storyapp.main_story.detail.DetailActivity
import com.example.storyapp.maps.MapsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var backPressedOnce = false
    private lateinit var adapter: StoryAdapter
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lac = LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.item_anim))
        lac.delay = 0.4f
        lac.order = LayoutAnimationController.ORDER_NORMAL

        adapter = StoryAdapter()
        binding.listStory.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.listStory.layoutAnimation = lac
        binding.listStory.adapter = adapter
        showStory()

        binding.addStory.setOnClickListener{
            intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }

        binding.showMaps.setOnClickListener{
            startActivity(Intent(this@MainActivity, MapsActivity::class.java))
        }

        binding.topAppBar.setOnMenuItemClickListener {MenuItem ->
            when(MenuItem.itemId){
                R.id.menu1 -> {
                    viewModel.logout()
                    startActivity(Intent(this@MainActivity, AuthenticationActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed()
            finishAffinity() // This will close the app
            return
        }

        backPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ backPressedOnce = false }, 2000)
    }

    private fun showStory(){

        viewModel.userSession.observe(this, Observer {
            adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickListener {
                override fun onItemClick(data: ListStoryItem) {
                    intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.EXTRA_ID, data.id)
                    intent.putExtra(DetailActivity.EXTRA_TOKEN, it.token)
                    startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity as Activity).toBundle())
                }
            })
        })

        viewModel.storyResponse.observe(this, Observer {
            adapter.submitData(lifecycle, it)
        })
    }
}

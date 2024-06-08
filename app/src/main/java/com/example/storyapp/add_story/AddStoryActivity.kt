package com.example.storyapp.add_story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.auth.AuthenticationViewModel
import com.example.storyapp.camera.CameraActivity
import com.example.storyapp.camera.CameraActivity.Companion.CAMERAX_RESULT
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.main_story.MainActivity

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddStoryBinding
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var currentImageUri: Uri? = null
    private var imageBitmap : Bitmap? = null
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.accessGallery.setOnClickListener {
            startGallery()
        }

        binding.accessCamera.setOnClickListener {
            startCameraX()
        }

        binding.buttonAdd.setOnClickListener {
            addStory()
        }
    }

    private fun startGallery(){
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            imageBitmap = viewModel.resizeAndCompressImage(uri, contentResolver, 1080, 1080, 1_000_000)
            binding.previewImageView.setImageBitmap(imageBitmap)
        }
    }

    private fun addStory(){
        val description = binding.edAddDescription.text.toString()
        viewModel.getSession().observe(this, Observer {
            if (it != null){
                viewModel.uploadStory(currentImageUri, this, description, token = it.token)
            }
        })

        viewModel.getResponse().observe(this, Observer {
            if (it.error == false){
                Toast.makeText(this, "Success Added Story", Toast.LENGTH_SHORT).show()
                intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed Added Story", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
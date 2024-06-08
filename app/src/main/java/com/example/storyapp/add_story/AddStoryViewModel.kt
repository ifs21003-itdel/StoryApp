package com.example.storyapp.add_story

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.repository.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.repository.response.AddStoryResponse
import com.example.storyapp.data.repository.retrofit.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import java.util.Locale

class AddStoryViewModel(private val repository: UserRepository) : ViewModel(){
    private val _addResponse = MutableLiveData<AddStoryResponse>()
    val addResponse: LiveData<AddStoryResponse> = _addResponse

    private val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    private val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

    private val _userSession = MutableLiveData<UserModel>()
    val userSession: LiveData<UserModel> get() = _userSession

    init {
        // Load the initial session from the repository
        viewModelScope.launch {
            repository.getSession().collect { session ->
                _userSession.postValue(session)
            }
        }
    }

    private fun uriToFile(imageUri: Uri, context: Context): File {
        val myFile = createCustomTempFile(context)
        val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
        val outputStream = FileOutputStream(myFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) outputStream.write(buffer, 0, length)
        outputStream.close()
        inputStream.close()
        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val filesDir = context.externalCacheDir
        return File.createTempFile(timeStamp, ".jpg", filesDir)
    }

    fun resizeAndCompressImage(uri: Uri, contentResolver: ContentResolver, maxWidth: Int, maxHeight: Int, maxFileSize: Long): Bitmap? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        var resizedBitmap = originalBitmap
        var streamLength = 0
        var compressQuality = 100
        val bmpStream = ByteArrayOutputStream()

        do {
            bmpStream.reset()
            resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            streamLength = bmpStream.size()
            compressQuality -= 5
        } while (streamLength >= maxFileSize && compressQuality > 0)

        return resizedBitmap
    }

    fun uploadStory(currentImageUri: Uri?, context: Context, description: String, lat: Float = 0.0F, lon: Float = 0.0F, token : String) {
        currentImageUri?.let { uri ->

            val imageFile = uriToFile(uri, context)

            val requestDesc = description.toRequestBody("text/plain".toMediaType())
            val requestLat = lat.toString().toRequestBody("text/plain".toMediaType())
            val requestLon = lon.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())

            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            ApiConfig.getApiService(context).postAddStory(
                "Bearer ${token}",
                requestDesc,
                multipartBody,
                requestLat,
                requestLon
            ).enqueue(object : Callback<AddStoryResponse> {
                override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                    if (response.isSuccessful) {
                        _addResponse.value = response.body()
                    } else {
                        Log.e("AddFailed", "Failed to Add Story")
                    }
                }

                override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                    Log.e("AddRequestFailed", "Request Add Story Failed")
                }
            })
        }
    }

    fun getResponse() : LiveData<AddStoryResponse> {
        return addResponse
    }

    fun getSession(): LiveData<UserModel> {
        return userSession
    }
}
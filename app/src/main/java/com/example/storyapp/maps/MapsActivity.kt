package com.example.storyapp.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.storyapp.R
import com.example.storyapp.ViewModelFactory
import com.example.storyapp.data.repository.response.LocatedListStoryItem

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.LatLngBounds

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val mapViewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mapViewModel.userSession.observe(this, Observer { session ->
            session?.let {
                Log.i("UserSession", "Token: ${it.token}")
                mapViewModel.getStoriesWithLocation(it.token, this)
            }
        })

        mapViewModel.listStoryResponse.observe(this, Observer {
            if (it != null) {
                addManyMarker(it)
            }
        })
    }

    private val boundsBuilder = LatLngBounds.Builder()

    private fun addManyMarker(stories: List<LocatedListStoryItem?>?) {
        stories?.forEach { story ->
            Log.i("ProcessingStory", "Story: $story")
            if (story != null && story.lat != null && story.lon != null) {
                val latLng = LatLng(story.lat, story.lon)
                Log.i("AddingMarker", "Lat: ${story.lat}, Lon: ${story.lon}, Name: ${story.name}")
                mMap.addMarker(MarkerOptions().position(latLng).title(story.name))
                boundsBuilder.include(latLng)
            } else {
                if (story == null) {
                    Log.w("StoryNull", "The Story is Empty")
                } else {
                    Log.w("InvalidLocation", "Story ${story.name} has invalid location data")
                }
            }
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                30
            )
        )
    }
}
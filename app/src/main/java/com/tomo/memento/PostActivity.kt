package com.tomo.memento

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.gestures
import com.tomo.memento.databinding.ActivityPostBinding

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private var imageUri: Uri? = null
    private var selectedLocation: Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get image URI from intent
        imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }

        imageUri?.let {
            binding.imagePreview.setImageURI(it)
        }

        setupMap()

        binding.postButton.setOnClickListener {
            val caption = binding.captionEditText.text.toString()
            if (selectedLocation == null) {
                Toast.makeText(this, "Please tap on the map to select a location.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Post submitted!", Toast.LENGTH_SHORT).show()

            // Go back to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupMap() {
        val mapboxMap = binding.mapView.mapboxMap

        val initialCamera = CameraOptions.Builder()
            .center(Point.fromLngLat(-98.0, 39.5)) // Center on US
            .zoom(3.0)
            .build()

        mapboxMap.setCamera(initialCamera)

        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
            binding.mapView.gestures.addOnMapClickListener { point ->
                selectedLocation = point
                Toast.makeText(
                    this,
                    "Location selected: ${point.latitude()}, ${point.longitude()}",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }
    }
}

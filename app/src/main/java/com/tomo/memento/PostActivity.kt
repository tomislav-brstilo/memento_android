package com.tomo.memento

import ApiService
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.gestures
import com.tomo.memento.databinding.ActivityPostBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

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

            if (imageUri == null) {
                Toast.makeText(this, "Image missing.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uniqueKey = "posts/${System.currentTimeMillis()}.jpg"

            // Prepare Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.mementoapp.eu/") // Or your server's base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            val inputStream = contentResolver.openInputStream(imageUri!!)
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = RequestBody.create(
                contentResolver.getType(imageUri!!)?.toMediaTypeOrNull(),
                tempFile
            )

            val body = MultipartBody.Part.createFormData("image", "photo.jpg", requestFile)

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userUid = currentUser?.uid ?: "anonymous"

            // existing bodies
            val captionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), caption)
            val latBody = RequestBody.create("text/plain".toMediaTypeOrNull(), selectedLocation!!.latitude().toString())
            val lonBody = RequestBody.create("text/plain".toMediaTypeOrNull(), selectedLocation!!.longitude().toString())
            val userBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userUid)

            apiService.uploadPost(body, captionBody, latBody, lonBody, userBody)
                .enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                        if (response.isSuccessful && response.body()?.ok == true) {
                            Toast.makeText(this@PostActivity, "Upload successful!", Toast.LENGTH_SHORT).show()
                            finish() // Or navigate elsewhere
                        } else {
                            Toast.makeText(this@PostActivity, "Upload failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        Toast.makeText(this@PostActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", t.message ?: "Unknown error")
                    }
                })


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

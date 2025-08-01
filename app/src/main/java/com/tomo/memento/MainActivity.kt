package com.tomo.memento

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.style
import com.tomo.memento.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.tomo.memento.ApiService
import com.tomo.memento.MessageResponse


class MainActivity : AppCompatActivity() {

    // ViewBinding to access views in activity_main.xml
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var photoUri: Uri? = null
    private val CAMERA_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // Use the correct binding for MainActivity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up MapView with initial camera and style
        setupMapView()

        // Handle bottom navigation item selection
        setupBottomNavigation()

        // Setup Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.mementoapp.eu/")  // HTTPS now!
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val apiService = retrofit.create(ApiService::class.java)

        // Make API call
        apiService.getHello().enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()!!.message
                    Toast.makeText(this@MainActivity, "Message from server: $message", Toast.LENGTH_LONG).show()
                    // Or update your UI here instead of Toast
                } else {
                    Toast.makeText(this@MainActivity, "Response failed: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API call failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun setupMapView() {
        val initialCamera = CameraOptions.Builder()
            .center(Point.fromLngLat(-98.0, 39.5)) // Longitude, Latitude
            .zoom(2.0)
            .bearing(0.0)
            .pitch(0.0)
            .build()

        val mapboxMap = binding.mapView.mapboxMap
        mapboxMap.setCamera(initialCamera)

        // Load the map style using the new StyleExtension DSL
        mapboxMap.loadStyle(
            style(Style.MAPBOX_STREETS) {
                // This block runs after style is loaded
                // Add layers or other customization here if needed
            }
        )
    }


    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    showExploreFilterMenu()
                    true
                }
                R.id.nav_messages -> {
                    // TODO: Implement Messages screen logic
                    true
                }
                R.id.nav_add -> {
                    showImageSourceDialog()
                    true
                }
                R.id.nav_notifications -> {
                    // TODO: Implement Notifications screen logic
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePhoto()
                1 -> pickPhotoFromGallery()
            }
        }
        builder.show()
    }

    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            return
        }

        val imageFile = File.createTempFile("photo_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun pickPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @Deprecated("Use Activity Result APIs for new apps")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    photoUri?.let {
                        val intent = Intent(this, PostActivity::class.java)
                        intent.putExtra("image_uri", it.toString())
                        startActivity(intent)
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        val intent = Intent(this, PostActivity::class.java)
                        intent.putExtra("image_uri", it.toString())
                        startActivity(intent)
                    }
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto() // Retry taking photo now that permission is granted
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExploreFilterMenu() {
        val bottomNavView = binding.bottomNavigation
        val exploreItemView = bottomNavView.findViewById<View>(R.id.nav_explore)

        val popup = PopupMenu(this, exploreItemView)
        popup.menuInflater.inflate(R.menu.explore_filter_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_friends -> {
                    // TODO: Filter posts by friends
                    true
                }
                R.id.filter_everyone -> {
                    // TODO: Filter posts by everyone in the world
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

}
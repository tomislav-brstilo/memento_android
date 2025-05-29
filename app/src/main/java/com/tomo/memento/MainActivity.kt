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
                        // TODO: Handle the captured photo URI
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        // TODO: Handle the selected image URI
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
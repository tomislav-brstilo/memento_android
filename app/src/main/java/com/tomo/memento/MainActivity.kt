package com.tomo.memento

import ApiService
import com.tomo.memento.Post
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
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

import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconRotationAlignment
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    // ViewBinding to access views in activity_main.xml
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var photoUri: Uri? = null
    private val CAMERA_PERMISSION_CODE = 1001

    private lateinit var apiService: ApiService
    private lateinit var pointAnnotationManager: PointAnnotationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // Setup Retrofit and ApiService once here to reuse
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.mementoapp.eu/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

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
            .center(Point.fromLngLat(-98.0, 39.5))
            .zoom(2.0)
            .build()

        val mapboxMap = binding.mapView.mapboxMap
        mapboxMap.setCamera(initialCamera)

        mapboxMap.loadStyle(style(Style.MAPBOX_STREETS) {
            loadPostsAndAddMarkers()
        })
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

    private fun fetchPostsAndAddMarkers() {
        apiService.getPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    val posts = response.body() ?: emptyList()
                    addMarkers(posts)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMarkers(posts: List<Post>) {
        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()

        posts.forEach { post ->
            val point = Point.fromLngLat(post.longitude, post.latitude)

            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage("ic_marker")  // see note below
            // optionally set icon size, anchor, etc.

            // You can customize the icon here by loading the image URL from post.imageUrl into a bitmap
            // and using it as a marker icon. For now, we use default marker.

            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private fun loadPostsAndAddMarkers() {
        apiService.getPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    response.body()?.let { posts ->
                        addMarkersForPosts(posts)
                    }
                }
            }
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun addMarkersForPosts(posts: List<Post>) {
        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()

        binding.mapView.getMapboxMap().getStyle { style ->
            posts.forEach { post ->
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = downloadBitmap(post.imageUrl)
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            val imageId = "post-image-${post.id}"
                            if (style.getStyleImage(imageId) == null) {
                                style.addImage(imageId, bitmap)
                            }

                            val pointAnnotationOptions = PointAnnotationOptions()
                                .withPoint(Point.fromLngLat(post.longitude, post.latitude))
                                .withIconImage(imageId)
                                .withIconSize(0.1)  // scale down the marker size (default is 1.0)

                            pointAnnotationManager.create(pointAnnotationOptions)
                        }
                    }
                }
            }
        }
    }


    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body?.byteStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
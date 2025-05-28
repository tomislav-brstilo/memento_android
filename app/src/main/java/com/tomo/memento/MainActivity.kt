package com.tomo.memento

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.style
import com.tomo.memento.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // ViewBinding to access views in activity_main.xml
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

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
                    // TODO: Implement Add action logic
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
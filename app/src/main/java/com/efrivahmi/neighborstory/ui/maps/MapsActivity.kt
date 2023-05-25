package com.efrivahmi.neighborstory.ui.maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.efrivahmi.neighborstory.R
import com.efrivahmi.neighborstory.data.response.ListStoryItem
import com.efrivahmi.neighborstory.databinding.ActivityMapsBinding
import com.efrivahmi.neighborstory.ui.main.MainViewModel
import com.efrivahmi.neighborstory.utils.NeighborFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var factory: NeighborFactory
    private var token = ""
    private val mainViewModel: MainViewModel by viewModels { factory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        factory = NeighborFactory.getInstance(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mainViewModel.getNeighbor().observe(this@MapsActivity) { result ->
            when (result) {
                is com.efrivahmi.neighborstory.utils.Result.Success -> {
                    token = result.data.token
                    getStories(token)
                }
                is com.efrivahmi.neighborstory.utils.Result.Error -> {
                    showErrorToast(result.error)
                }
                else -> {}
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun showErrorToast(errorMessage: String) {
        Toast.makeText(this@MapsActivity, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setupList()
        getMyLocation()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                else -> {
                }
            }
        }

    private fun setupList() {
        mainViewModel.listNeighbor.observe(this@MapsActivity) { result ->
            when (result) {
                is com.efrivahmi.neighborstory.utils.Result.Success -> {
                    val story = result.data
                    story?.listStory?.forEach { list ->
                        mMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(list.lat, list.lon))
                                .title("Story from : ${list.name}")
                                .snippet("ID : ${list.id}")
                        )
                    }
                }
                is com.efrivahmi.neighborstory.utils.Result.Error -> {
                    showErrorToast(result.error)
                }
                else -> {
                }
            }
        }
    }

    private fun getStories(token: String) {
        mainViewModel.createStory(token)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {
        if     (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker()
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showStartMarker() {
        val data = intent.getParcelableArrayListExtra<ListStoryItem>("location")
        data?.map {
            if (it.lat != null && it.lon != null) {
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat, it.lon))
                        .title(it.name)
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-1.0,70.0), 1f))
            }
        }
    }
}


package com.example.funny_2.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.funny_2.R
import com.example.funny_2.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    companion object {
        private const val EXTRA_LOCATION_ID = "EXTRA_LOCATION_ID"
        private const val EXTRA_LAT_ID = "EXTRA_LAT_ID"
        private const val EXTRA_LON_ID = "EXTRA_LON_ID"

        fun create(context: Context, locationID: String, latID: Double, lonID: Double): Intent {
            return Intent(context, MapsActivity::class.java).apply {
                putExtra(EXTRA_LOCATION_ID, locationID)
                putExtra(EXTRA_LAT_ID, latID)
                putExtra(EXTRA_LON_ID, lonID)
            }
        }
    }

    private var locationID: String = ""
    private var latID: Double = 0.0
    private var lonID: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationID = intent.getStringExtra(EXTRA_LOCATION_ID).toString()
        latID = intent.getDoubleExtra(EXTRA_LAT_ID, 0.0)
        lonID = intent.getDoubleExtra(EXTRA_LON_ID, 0.0)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val location = LatLng(latID, lonID)
        mMap.addMarker(MarkerOptions().position(location).title(locationID))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}
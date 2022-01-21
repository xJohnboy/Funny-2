package com.example.funny_2.ui.map

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.funny_2.R
import com.example.funny_2.data.Entities
import com.example.funny_2.data.MapData
import com.example.funny_2.data.api.ApiMap
import kotlinx.android.synthetic.main.fragment_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NearbyMapActivity : AppCompatActivity() {
    private val apiService = ApiMap()
    private val adapter = MapAdapter()
    private var hashMapDouble = hashMapOf<String, Double>()

    companion object {
        private const val NEARBY_LOCATION = "Nearby Location."
        private const val EXTRA_LAT_ID = "EXTRA_LAT_ID"
        private const val EXTRA_LON_ID = "EXTRA_LON_ID"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"

        fun create(context: Context, latitude: Double, longitude: Double): Intent {
            return Intent(context, NearbyMapActivity::class.java).apply {
                putExtra(EXTRA_LAT_ID, latitude)
                putExtra(EXTRA_LON_ID, longitude)
            }
        }
    }

    private var latitude: Double = -1.0
    private var longitude: Double = -1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        latitude = intent.getDoubleExtra(EXTRA_LAT_ID, -1.0)
        longitude = intent.getDoubleExtra(EXTRA_LON_ID, -1.0)

        setContentView(R.layout.fragment_map)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerMap?.layoutManager = LinearLayoutManager(this)
        recyclerMap?.adapter = adapter
        recyclerMap?.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        adapter.onViewClick = { data ->
            startActivity(
                MapsActivity.create(
                    this,
                    data.store_name,
                    data.latitude,
                    data.longitude
                )
            )
        }

        swipeToRefreshMap?.setOnRefreshListener {
            loadData()
        }

        title = NEARBY_LOCATION
        loadData()
    }

    private fun loadData() {
        adapter.clear()
        swipeToRefreshMap?.isRefreshing = true
        hashMapDouble[LATITUDE] = latitude
        hashMapDouble[LONGITUDE] = longitude
        fetchMap()
    }

    private fun fetchMap() {
        val call = apiService.getNearbyMap(hashMapDouble)
        call.enqueue(object : Callback<MapData> {
            override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                if (response.isSuccessful) {
                    response.body()?.entities?.let { handleMapData(it) }
                }
                swipeToRefreshMap?.isRefreshing = false
            }

            override fun onFailure(call: Call<MapData>, t: Throwable) {
                swipeToRefreshMap?.isRefreshing = false
                Log.e("Map API", t.message.toString())
            }
        })
    }

    private fun handleMapData(item: ArrayList<Entities>) {
        adapter.addMap(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
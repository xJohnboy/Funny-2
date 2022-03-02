package com.example.funny_2.ui.map

import android.app.Activity
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_2.R
import com.example.funny_2.data.Entities
import com.example.funny_2.data.MapData
import com.example.funny_2.data.Page_information
import com.example.funny_2.data.api.ApiMap
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.location.config.LocationParams
import kotlinx.android.synthetic.main.fragment_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MapFragment : Fragment(), OnLocationUpdatedListener {
    private val apiService = ApiMap()
    private val adapter = MapAdapter()
    private var hashMapAny = hashMapOf<String, Any?>()
    private var isLoading = false
    private var isSearching = false

    companion object {
        private const val KEY_FILTER_TYPE = "filter_type"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val VALUE_FILTER_TYPE = "1"
        private const val KEY_PAGE = "page"
        private const val KEY_Q = "q"
    }

    private var latitude: Double? = null
    private var longitude: Double? = null
    lateinit var searchString: String
    private var pageLimit = 1
    private var page = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.bottom_search, menu)
        val item = menu.findItem(R.id.bottom_search)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.clear()
                page = 1
                if (newText != null) {
                    if (newText.isNotEmpty()) {
                        isSearching = true
                        searchString = newText.lowercase(Locale.getDefault())
                        loadData()
                    } else {
                        isSearching = false
                        loadData()
                    }
                }
                return true
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerMap?.layoutManager = LinearLayoutManager(activity)
        recyclerMap?.adapter = adapter
        recyclerMap?.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerMap?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    if (!isLoading)
                        if (!recyclerView.canScrollVertically(1))
                            if (page <= pageLimit) {
                                loadData()
                            } else {
                                scrollUpMap?.visibility = View.VISIBLE
                            }
                }
                if (dy < 0)
                    scrollUpMap?.visibility = View.GONE
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        scrollUpMap?.setOnClickListener {
            recyclerMap?.smoothScrollToPosition(0)
        }

        adapter.onViewClick = { data ->
            startActivity(
                MapsActivity.create(
                    requireActivity(),
                    data.store_name,
                    data.latitude,
                    data.longitude
                )
            )
        }

        swipeToRefreshMap?.setOnRefreshListener {
            page = 1
            loadData()
        }

        getPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val getPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkLocationEnableOrNot()
        } else {
            loadData()
        }
    }

    private fun checkLocationEnableOrNot() {

        requireActivity().let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority =
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val states = response.locationSettingsStates
                if (states.isLocationPresent) {
                    smartLocation()
                }
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        val intentSenderRequest =
                            e.status.resolution?.let { it ->
                                IntentSenderRequest.Builder(it).build()
                            }
                        launcherAfterLocationChecked.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }

    private val launcherAfterLocationChecked = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            smartLocation()
        } else {
            loadData()
        }
    }

    private fun smartLocation() {
        swipeToRefreshMap?.isRefreshing = true
        SmartLocation
            .with(requireActivity())
            .location()
            .oneFix()
            .config(LocationParams.NAVIGATION)
            .start { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    loadData()
                }
            }
    }

    private fun loadData() {
        scrollUpMap?.visibility = View.GONE

        if (page == 1) {
            swipeToRefreshMap?.isRefreshing = true
        } else {
            progressBarMap?.visibility = View.VISIBLE
        }

        hashMapAny.clear()
        hashMapAny[KEY_FILTER_TYPE] = VALUE_FILTER_TYPE
        hashMapAny[KEY_PAGE] = "$page"
        isLoading = true
        if (latitude != null && longitude != null) {
            hashMapAny[KEY_LATITUDE] = latitude
            hashMapAny[KEY_LONGITUDE] = longitude
        }
        if (isSearching) {
            hashMapAny[KEY_Q] = searchString
        }

        fetchMap()
    }

    private fun fetchMap() {
        val call = apiService.getMap(hashMapAny)
        call.enqueue(object : Callback<MapData> {
            override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                if (response.isSuccessful) {
                    progressBarMap?.visibility = View.GONE
                    swipeToRefreshMap?.isRefreshing = false
                    isLoading = false
                    response.body()?.entities?.let { handleMapData(it) }
                    response.body()?.page_information?.let { handlePageInformation(it) }
                    page++
                }
            }

            override fun onFailure(call: Call<MapData>, t: Throwable) {
                swipeToRefreshMap?.isRefreshing = false
                Log.e("Map API", t.message.toString())
                Toast.makeText(
                    requireActivity(),
                    t.message.toString(),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun handleMapData(item: ArrayList<Entities>) {
        if (page == 1) {
            adapter.clear()
        }
        adapter.addMap(item)
    }

    private fun handlePageInformation(getPage: Page_information) {
        page = getPage.page
        pageLimit = getPage.number_of_page
    }

    override fun onLocationUpdated(location: Location?) {
    }
}
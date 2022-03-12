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
import kotlin.collections.ArrayList

class MapFragment : Fragment(), OnLocationUpdatedListener {
    private val apiService = ApiMap()
    private val adapter = MapAdapter()

    private var isLoading = false
    private var pageLimit = 1
    private var page = 1

    private var currentLocation: Location? = null
    private var searchString: String? = null

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
                newText?.let {
                    searchString = if (it.isNotEmpty()) it else null
                } ?: kotlin.run {
                    searchString = null
                }

                loadData(1)

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
                                loadData(page + 1)
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
            loadData(1)
        }

        getPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val getPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkLocationEnableOrNot()
        } else {
            loadData(1)
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
            loadData(1)
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
                    currentLocation = location
                    loadData(1)
                }
            }
    }

    private fun loadData(page: Int) {
        scrollUpMap?.visibility = View.GONE
        isLoading = true

        if (page == 1) {
            swipeToRefreshMap?.isRefreshing = true
        } else {
            progressBarMap?.visibility = View.VISIBLE
        }

        val call = apiService.getMap(
            "1",
            page,
            currentLocation?.latitude,
            currentLocation?.longitude,
            searchString
        )

        call.enqueue(object : Callback<MapData> {
            override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                isLoading = false
                swipeToRefreshMap?.isRefreshing = false
                progressBarMap?.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.page_information?.let { handlePageInformation(it) }
                    response.body()?.entities?.let { handleMapData(it) }
                }
            }

            override fun onFailure(call: Call<MapData>, t: Throwable) {
                swipeToRefreshMap?.isRefreshing = false
                progressBarMap?.visibility = View.GONE
                isLoading = false
                Log.e("Map API", t.message.toString())
                Toast.makeText(
                    requireActivity(),
                    t.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
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
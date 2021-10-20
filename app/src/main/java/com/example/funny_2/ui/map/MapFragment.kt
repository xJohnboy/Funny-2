package com.example.funny_2.ui.map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.funny_2.R
import com.example.funny_2.data.Entities
import com.example.funny_2.data.MapData
import com.example.funny_2.data.Page_information
import com.example.funny_2.data.api.ApiMap
import kotlinx.android.synthetic.main.fragment_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class MapFragment : Fragment() {
    private val apiService = ApiMap()
    private val adapter = MapAdapter()
    private var pageLimit = 1
    private var page = 1
    private var hashMap = hashMapOf<String, String>()
    private var isLoading = false

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
            startActivity(MapsActivity.create(requireContext(),data.store_name,data.latitude,data.longitude))
        }

        swipeToRefreshMap?.setOnRefreshListener {
            page = 1
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        if (page == 1) {
            swipeToRefreshMap?.isRefreshing = true
        } else {
            progressBarMap?.visibility = View.VISIBLE
        }
        hashMap["filter_type"] = "1"
        hashMap["page"] = "$page"
        isLoading = true

        fetchMap()
    }

    private fun fetchMap() {
        val call = apiService.getMap(hashMap)
        call.enqueue(object : Callback<MapData> {
            override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                if (response.isSuccessful) {
                    response.body()?.entities?.let { handleMapData(it) }
                    response.body()?.page_information?.let { handlePageInformation(it) }
                    page++
                    isLoading = false
                    progressBarMap?.visibility = View.GONE
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
        if (page == 1) {
            adapter.clear()
        }
        adapter.addMap(item)
    }

    private fun handlePageInformation(getPage: Page_information) {
        page = getPage.page
        pageLimit = getPage.number_of_page
    }
}
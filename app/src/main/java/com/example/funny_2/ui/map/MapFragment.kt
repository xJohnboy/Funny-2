package com.example.funny_2.ui.map

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.SearchView
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
import java.util.*
import kotlin.collections.ArrayList

class MapFragment : Fragment() {
    private val apiService = ApiMap()
    private val adapter = MapAdapter()
    private var pageLimit = 1
    private var page = 1
    private var hashMap = hashMapOf<String, String>()
    private var isLoading = false
    private var listMap = ArrayList<Entities>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeToRefreshMap?.setOnRefreshListener {
            swipeToRefreshMap?.isRefreshing = true
            Handler(Looper.getMainLooper()).postDelayed({
                adapter.clear()
                page = 1
                fetchMap()
            }, 1000)
        }
        initMapAllPage()
        fetchMap()
        progressBarMap?.visibility = View.GONE
    }

    private fun fetchMap() {
        hashMap["filter_type"] = "1"
        hashMap["page"] = "$page"
        isLoading = true
        val call = apiService.getMap(hashMap)
        call.enqueue(object : Callback<MapData> {
            override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                if (response.isSuccessful) {
                    adapter.clear()
                    showMap(response.body()!!.entities)
                    addPageInfo(response.body()!!.page_information)
                    progressBarMap?.visibility = View.GONE
                    page++
                    isLoading = false
                    swipeToRefreshMap?.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<MapData>, t: Throwable) {
                Log.e("Map API", t.message.toString())
                swipeToRefreshMap?.isRefreshing = false
            }
        })
    }

    private fun showMap(item: ArrayList<Entities>) {
        recyclerMap?.layoutManager = LinearLayoutManager(activity)
        recyclerMap?.addItemDecoration(DividerItemDecoration(activity,DividerItemDecoration.VERTICAL))
        recyclerMap?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    if (!isLoading)
                        if (!recyclerView.canScrollVertically(1))
                            if (page <= pageLimit) {
                                addMapToPage()
                            } else {
                                scrollUpMap?.visibility = View.VISIBLE
                            }
                }
                if (dy < 0)
                    scrollUpMap?.visibility = View.GONE
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        adapter.addMap(item)
        recyclerMap?.adapter = adapter
        adapter.onViewClick = { data ->
            val itn = Intent(activity, MapsActivity::class.java).apply {
                putExtra("location", data.store_name)
                putExtra("lat", data.latitude)
                putExtra("lon", data.longitude)
            }
            startActivity(itn)
        }
        scrollUpMap?.setOnClickListener {
            recyclerMap?.smoothScrollToPosition(0)
            scrollUpMap?.visibility = View.GONE
        }
    }

    private fun addPageInfo(getPage: Page_information) {
        page = getPage.page
        pageLimit = getPage.number_of_page
    }

    private fun addMapToPage() {
        hashMap["filter_type"] = "1"
        hashMap["page"] = "$page"
        val call = apiService.getMap(hashMap)
        isLoading = true
        progressBarMap?.visibility = View.VISIBLE
        call.enqueue(object : Callback<MapData> {
            override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                if (response.isSuccessful) {
                    adapter.addMap(response.body()!!.entities)
                    progressBarMap?.visibility = View.GONE
                    isLoading = false
                    swipeToRefreshMap?.isRefreshing = false
                    page++
                }
            }

            override fun onFailure(call: Call<MapData>, t: Throwable) {
                Log.e("Map API", t.message.toString())
            }
        })
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
                if (newText!!.isNotEmpty()) {
                    adapter.clear()
                    val search = newText.lowercase(Locale.getDefault())
                    listMap.forEach {
                        if (it.store_name.lowercase(Locale.getDefault()).contains(search)) {
                            adapter.addListMap(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.clear()
                    page = 1
                    fetchMap()
                }
                adapter.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun initMapAllPage() {
        for (i in 1..13) {
            hashMap["filter_type"] = "1"
            hashMap["page"] = "$i"
            val call = apiService.getMap(hashMap)
            call.enqueue(object : Callback<MapData> {
                override fun onResponse(call: Call<MapData>, response: Response<MapData>) {
                    if (response.isSuccessful)
                        listMap.addAll(response.body()!!.entities)
                }

                override fun onFailure(call: Call<MapData>, t: Throwable) {
                    Log.e("Map API", t.message.toString())
                }
            })
        }
    }
}
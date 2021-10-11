package com.example.funny_2.ui.store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_2.R
import com.example.funny_2.data.StoreData
import com.example.funny_2.data.api.ApiStore
import kotlinx.android.synthetic.main.fragment_store.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class StoreFragment : Fragment() {
    private val apiService = ApiStore()
    private val adapter = StoreAdapter()
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var category = "x"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.bottom_sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_sort -> {
                val itn = Intent(activity, CategoryActivity::class.java)
                itn.putExtra("category",category)
                activityResultLauncher.launch(itn)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                if (result!!.resultCode == Activity.RESULT_OK) {
                    category = result.data!!.extras!!.getString("category").toString()
                    adapter.clear()
                    if (category == "All item") {
                        swipeToRefreshStore?.setOnRefreshListener {
                            swipeToRefreshStore?.isRefreshing = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                adapter.clear()
                                fetchStore()
                            }, 1000)
                        }
                        fetchStore()
                    } else {
                        swipeToRefreshStore?.setOnRefreshListener {
                            swipeToRefreshStore?.isRefreshing = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                adapter.clear()
                                fetchStoreByCategory(category)
                            }, 1000)
                        }
                        fetchStoreByCategory(category)
                    }
                }
            }
        swipeToRefreshStore?.setOnRefreshListener {
            swipeToRefreshStore?.isRefreshing = true
            Handler(Looper.getMainLooper()).postDelayed({
                adapter.clear()
                fetchStore()
            }, 1000)
        }
        fetchStore()
    }

    private fun fetchStoreByCategory(category: String) {
        val call = apiService.getStoreByCategory(category)
        call.enqueue(object : Callback<ArrayList<StoreData>> {
            override fun onResponse(
                call: Call<ArrayList<StoreData>>,
                response: Response<ArrayList<StoreData>>
            ) {
                if (response.isSuccessful)
                    showStore(response.body()!!)
                swipeToRefreshStore?.isRefreshing = false

            }

            override fun onFailure(call: Call<ArrayList<StoreData>>, t: Throwable) {
                Log.e("Store API", t.message.toString())
                swipeToRefreshStore?.isRefreshing = false
            }
        })
    }

    private fun fetchStore() {
        val call = apiService.getProduct()
        call.enqueue(object : Callback<ArrayList<StoreData>> {
            override fun onResponse(
                call: Call<ArrayList<StoreData>>,
                response: Response<ArrayList<StoreData>>
            ) {
                if (response.isSuccessful)
                    showStore(response.body()!!)
                swipeToRefreshStore?.isRefreshing = false

            }

            override fun onFailure(call: Call<ArrayList<StoreData>>, t: Throwable) {
                Log.e("Store API", t.message.toString())
                swipeToRefreshStore?.isRefreshing = false
            }
        })
    }

    private fun showStore(item: ArrayList<StoreData>) {
        recyclerViewStore?.layoutManager = GridLayoutManager(activity, 2)
        recyclerViewStore?.adapter = adapter
        adapter.addProduct(item)
        adapter.onViewClick = { data ->
            val itnToProductDetail = Intent(activity, ProductDetailActivity::class.java).apply {
                putExtra("id", data.id)
                putExtra("product_title", data.title)
            }
            startActivity(itnToProductDetail)
        }
    }
}
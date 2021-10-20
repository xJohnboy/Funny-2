package com.example.funny_2.ui.store

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_2.R
import com.example.funny_2.data.StoreData
import com.example.funny_2.data.api.ApiStore
import com.example.funny_2.ui.store.`interface`.OnCategoryActivityResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_store.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class StoreFragment : Fragment() {
    private val apiService = ApiStore()
    private val adapter = StoreAdapter()
    var selectedCategory = CategoryActivity.ALL_ITEMS

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
                showCategory()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewStore?.layoutManager = GridLayoutManager(activity, 2)
        recyclerViewStore?.adapter = adapter
        adapter.onViewClick = { data ->
            startActivity(ProductDetailActivity.create(requireContext(), data.id))
        }

        swipeToRefreshStore?.setOnRefreshListener {
            loadData()
        }

        loadData()
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            CategoryActivity.onActivityResult(
                result?.resultCode,
                result?.data,
                object : OnCategoryActivityResult {
                    override fun didSelectCategory(category: String) {
                        adapter.clear()
                        this@StoreFragment.selectedCategory = category
                        loadData()
                    }
                })
        }

    private fun showCategory() {
        activityResultLauncher.launch(CategoryActivity.create(requireContext(), selectedCategory))
    }

    private fun loadData() {
        swipeToRefreshStore?.isRefreshing = true
        if (selectedCategory == CategoryActivity.ALL_ITEMS) {
            fetchStore()
            (activity as AppCompatActivity).title = "Store"
        } else {
            fetchStoreByCategory()
            (activity as AppCompatActivity).title = selectedCategory.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    }

    private fun fetchStoreByCategory() {
        val call = apiService.getStoreByCategory(selectedCategory)
        call.enqueue(object : Callback<ArrayList<StoreData>> {
            override fun onResponse(
                call: Call<ArrayList<StoreData>>,
                response: Response<ArrayList<StoreData>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { handleStoreData(it) }
                }
                swipeToRefreshStore?.isRefreshing = false

            }

            override fun onFailure(call: Call<ArrayList<StoreData>>, t: Throwable) {
                swipeToRefreshStore?.isRefreshing = false
                Log.e("Store API", t.message.toString())
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
                swipeToRefreshStore?.isRefreshing = false
                if (response.isSuccessful) {
                    response.body()?.let { handleStoreData(it) }
                }
            }

            override fun onFailure(call: Call<ArrayList<StoreData>>, t: Throwable) {
                swipeToRefreshStore?.isRefreshing = false
                Log.e("Store API", t.message.toString())
            }
        })
    }

    private fun handleStoreData(item: ArrayList<StoreData>) {
        adapter.clear()
        adapter.addProduct(item)
    }
}
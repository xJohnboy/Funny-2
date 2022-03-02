package com.example.funny_2.ui.store

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_2.R
import com.example.funny_2.data.api.ApiStore
import com.example.funny_2.ui.store.`interface`.OnCategoryActivityResult
import kotlinx.android.synthetic.main.activity_category.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class CategoryActivity : AppCompatActivity() {
    private val apiService = ApiStore()
    private val adapter = CategoryAdapter()
    private var selectedCategory = ALL_ITEMS

    companion object {
        private const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
        private const val ARE_YOU_LOOKING_FOR = "Are you looking for ?"

        const val ALL_ITEMS = "All item"

        fun create(context: Context, selectedCategory: String): Intent {
            return Intent(context, CategoryActivity::class.java).apply {
                putExtra(EXTRA_CATEGORY, selectedCategory)
            }
        }

        fun onActivityResult(
            resultCode: Int?,
            data: Intent?,
            onCategoryActivityResult: OnCategoryActivityResult?
        ) {
            when (resultCode) {
                RESULT_OK -> {
                    val category = data?.getStringExtra(EXTRA_CATEGORY)
                    category?.let { onCategoryActivityResult?.didSelectCategory(it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCategory = intent?.getStringExtra(EXTRA_CATEGORY) ?: ALL_ITEMS

        setContentView(R.layout.activity_category)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter.categorySelected = selectedCategory
        recyclerCategory.layoutManager = GridLayoutManager(this, 1)
        recyclerCategory.adapter = adapter
        adapter.onViewClick = { data ->
            didSelectCategory(data)
        }

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        title = ARE_YOU_LOOKING_FOR
        loadData()
    }

    private fun loadData() {
        swipeRefreshLayout.isRefreshing = true
        val call = apiService.getCategory()
        call.enqueue(object : Callback<ArrayList<String>> {
            override fun onResponse(
                call: Call<ArrayList<String>>, response: Response<ArrayList<String>>
            ) {
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    response.body()?.let { adapter.addAll(it) }
                }
            }

            override fun onFailure(call: Call<ArrayList<String>>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Log.e("API", t.message.toString())
            }
        })
    }

    private fun didSelectCategory(category: String) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_CATEGORY, category)
        })

        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
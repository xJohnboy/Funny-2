package com.example.funny_2.ui.store

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funny_2.R
import com.example.funny_2.data.api.ApiStore
import kotlinx.android.synthetic.main.activity_category.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class CategoryActivity : AppCompatActivity() {
    private val apiService = ApiStore()
    private val adapter = CategoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        val call = apiService.getCategory()
        call.enqueue(object : Callback<ArrayList<String>> {
            override fun onResponse(
                call: Call<ArrayList<String>>, response: Response<ArrayList<String>>
            ) {
                if (response.isSuccessful)
                    showCategory(response.body()!!)
            }

            override fun onFailure(call: Call<ArrayList<String>>, t: Throwable) {
                Log.e("API", t.message.toString())
            }
        })
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun showCategory(item:ArrayList<String>){
        val category = intent.getStringExtra("category")
        adapter.categorySelected = category.toString()
        adapter.addAll(item)
        recyclerCategory.layoutManager = GridLayoutManager(this,1)
        recyclerCategory.adapter = adapter
        adapter.onViewClick = { data ->
            val itn = Intent()
            itn.putExtra("category",data)
            setResult(Activity.RESULT_OK,itn)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
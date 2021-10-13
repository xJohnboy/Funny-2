package com.example.funny_2.ui.store

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.funny_2.R
import com.example.funny_2.data.StoreData
import com.example.funny_2.data.api.ApiStore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {
    private val apiService = ApiStore()

    companion object {
        private const val EXTRA_PRODUCT_ID = "EXTRA_PRODUCT_ID"

        fun create(context: Context, productId: Int): Intent {
            return Intent(context, ProductDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
            }
        }
    }

    private var productId: Int = -1
    private var storeData: StoreData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)

        setContentView(R.layout.activity_product_detail)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        loadData()
    }

    private fun loadData() {
        val call = apiService.getProductDetail(productId)
        call.enqueue(object : Callback<StoreData> {
            override fun onResponse(call: Call<StoreData>, response: Response<StoreData>) {
                if (response.isSuccessful) {
                    this@ProductDetailActivity.storeData = response.body()
                    updateUI()
                }
            }

            override fun onFailure(call: Call<StoreData>, t: Throwable) {
                Log.e("API",t.message.toString())
            }
        })
    }

    private fun updateUI() {
        title = storeData?.title
        Picasso.get().load(storeData?.image).into(imageProductDetail)
        textTitleDetail.text = storeData?.title
        textDesDetail.text = storeData?.description
        textPriceDetail.text = "$ ${storeData?.price}"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
package com.example.funny_2.ui.store

import android.annotation.SuppressLint
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        intent.apply {
            val productId = getIntExtra("id", 0)
            val productTitle = getStringExtra("product_title").toString()
            title = productTitle
            val call = apiService.getProductDetail(productId)
            call.enqueue(object : Callback<StoreData> {
                override fun onResponse(call: Call<StoreData>, response: Response<StoreData>) {
                    if (response.isSuccessful)
                        showDetail(response.body()!!)
                }

                override fun onFailure(call: Call<StoreData>, t: Throwable) {
                    Log.e("API",t.message.toString())
                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDetail(item: StoreData){
        Picasso.get().load(item.image).into(imageProductDetail)
        textTitleDetail.text = item.title
        textDesDetail.text = item.description
        textPriceDetail.text = "$ ${item.price}"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
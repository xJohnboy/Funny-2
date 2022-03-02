package com.example.funny_2.data.api

import com.example.funny_2.data.MapData
import com.example.funny_2.data.StoreData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

const val STORE_URL = "https://fakestoreapi.com/"
const val MAP_URL = "http://dev.brtmobile.com/api/v1/branches/"

interface ApiStore {
    @GET("products")
    fun getProduct(): Call<ArrayList<StoreData>>

    @GET("products/{id}")
    fun getProductDetail(@Path("id") id: Int): Call<StoreData>

    @GET("products/categories")
    fun getCategory(): Call<ArrayList<String>>

    @GET("products/category/{category}")
    fun getStoreByCategory(@Path("category") category: String): Call<ArrayList<StoreData>>

    companion object {
        operator fun invoke(): ApiStore {
            return Retrofit.Builder()
                .baseUrl(STORE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiStore::class.java)
        }
    }
}

interface ApiMap {
    @GET("mobile")
    fun getMap(@QueryMap map: HashMap<String, Any?>): Call<MapData>

    companion object {
        operator fun invoke(): ApiMap {
            return Retrofit.Builder()
                .baseUrl(MAP_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiMap::class.java)
        }
    }
}
package com.example.funny_2.data

import android.media.Rating

data class StoreData(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating
)

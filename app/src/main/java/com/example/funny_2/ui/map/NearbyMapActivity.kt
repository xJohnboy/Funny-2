package com.example.funny_2.ui.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.funny_2.R
import com.example.funny_2.data.api.ApiMap

class NearlyMapActivity : AppCompatActivity() {
    private val apiService = ApiMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_map)
    }
}
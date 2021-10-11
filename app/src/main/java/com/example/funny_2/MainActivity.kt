package com.example.funny_2

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.example.funny_2.databinding.ActivityMainBinding
import com.example.funny_2.ui.account.AccountFragment
import com.example.funny_2.ui.map.MapFragment
import com.example.funny_2.ui.store.StoreFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recycler_store.view.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Store"

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_store -> {
                    title = "Store"
                    viewPager.setCurrentItem(0, false)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_map -> {
                    title = "Map"
                    viewPager.setCurrentItem(1, false)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_account -> {
                    title = "Account"
                    viewPager.setCurrentItem(2, false)
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        initFragment()
        viewPager.adapter = viewPagerAdapter
        viewPager.isUserInputEnabled = false
    }

    private fun initFragment() {
        viewPagerAdapter.addFragment(StoreFragment())
        viewPagerAdapter.addFragment(MapFragment())
        viewPagerAdapter.addFragment(AccountFragment())
    }
}
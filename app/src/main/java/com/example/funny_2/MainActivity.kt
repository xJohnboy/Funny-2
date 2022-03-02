package com.example.funny_2

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.example.funny_2.databinding.ActivityMainBinding
import com.example.funny_2.ui.account.AccountFragment
import com.example.funny_2.ui.map.MapFragment
import com.example.funny_2.ui.store.StoreFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val PAGE_STORE_TITLE = "Store"
        const val PAGE_MAP_TITLE = "Map"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
    private var currentStoreFragmentTitle = PAGE_STORE_TITLE
    private var currentMapFragmentTitle = PAGE_MAP_TITLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = PAGE_STORE_TITLE

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_store -> {
                    title = currentStoreFragmentTitle
                    viewPager?.setCurrentItem(0, false)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_map -> {
                    title = currentMapFragmentTitle
                    viewPager?.setCurrentItem(1, false)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_account -> {
                    title = "Account"
                    viewPager?.setCurrentItem(2, false)
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }
        initFragment()
        viewPager.adapter = viewPagerAdapter
        viewPager.isUserInputEnabled = false
    }

    fun updateStoreTitle(title: String) {
        currentStoreFragmentTitle = title
        this.title = title
    }

    private fun initFragment() {
        viewPagerAdapter.addFragment(StoreFragment())
        viewPagerAdapter.addFragment(MapFragment())
        viewPagerAdapter.addFragment(AccountFragment())
    }
}
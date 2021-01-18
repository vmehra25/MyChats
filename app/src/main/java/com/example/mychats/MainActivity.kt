package com.example.mychats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mychats.adapters.ScreenSliderAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        viewPager.adapter = ScreenSliderAdapter(this)


        TabLayoutMediator(tabs, viewPager) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> {
                    tab.text = "CHATS"
                }
                1 -> {
                    tab.text = "PEOPLE"
                }
            }
        }.attach()



    }

}



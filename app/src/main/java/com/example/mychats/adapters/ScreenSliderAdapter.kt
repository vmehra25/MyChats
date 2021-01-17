package com.example.mychats.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mychats.fragments.PeopleFragment
import com.example.mychats.fragments.InboxFragment

class ScreenSliderAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount() = 2


    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                InboxFragment()
            }
            else -> {
                PeopleFragment()
            }
        }
    }

}

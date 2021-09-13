package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_splash

import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class InstapayViewPagerAdapter (
    val items: ArrayList<Fragment>,
    activity: AppCompatActivity
    ) : FragmentStateAdapter(activity){

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }

}
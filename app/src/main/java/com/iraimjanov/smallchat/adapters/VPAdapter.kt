package com.iraimjanov.smallchat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VPAdapter(
    var listFragment: List<Fragment>,
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return listFragment.size
    }

    override fun createFragment(position: Int): Fragment {
        return listFragment[position]
    }

    override fun getItemId(position: Int): Long {
        return listFragment[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return listFragment.find { it.hashCode().toLong() == itemId } != null
    }

}
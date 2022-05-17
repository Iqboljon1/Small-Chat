package com.iraimjanov.smallchat.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.iraimjanov.smallchat.adapters.VPAdapter
import com.iraimjanov.smallchat.cc.CountryDB
import com.iraimjanov.smallchat.data.BuildFAB
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.data.UpdateUser
import com.iraimjanov.smallchat.db.AppDatabase
import com.iraimjanov.smallchat.R
import com.iraimjanov.smallchat.activities.MainActivity
import com.iraimjanov.smallchat.databinding.DialogLogoutBinding
import com.iraimjanov.smallchat.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var appDatabase: AppDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()) {
            UpdateUser().updateUser()
            connection()
        } else {
            findNavController().navigate(R.id.action_homeFragment_to_noInternetFragment)
        }

        return binding.root
    }

    @SuppressLint("RtlHardcoded")
    private fun connection() {
        loadData()
        BuildFAB(requireActivity(), binding, findNavController()).build()
        buildVP()
        settingDrawerLayoutChildViews()
        binding.imageIcOpenDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(Gravity.LEFT)
        }
        binding.lySettingProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_editProfileFragment)
            binding.drawerLayout.close()
        }
        binding.lyLogout.setOnClickListener {
            buildDialogLogout()
            binding.drawerLayout.close()
        }
        binding.imageSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                settingDrawerLayoutChildViews()
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun loadData() {
        appDatabase = AppDatabase.getInstance(requireActivity())
        PublicData.countryDB = CountryDB("Uzbekistan", "+998")
    }

    private fun buildVP() {
        val listFragment = listOf(PersonalChatFragment(), GroupsChatFragment())
        binding.viewPager.adapter = VPAdapter(listFragment, requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_tab_user)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_tab_groups)
                }
            }
        }.attach()
    }

    @SuppressLint("SetTextI18n")
    private fun settingDrawerLayoutChildViews() {
        binding.tvName.text = "${profile.firstName} ${profile.lastName}"
        binding.tvNumber.text = profile.number
        if (profile.imageId.isNotEmpty()) {
            Glide.with(binding.root).load(profile.imageUrl).centerCrop().into(binding.imageProfile)
        }
    }

    private fun buildDialogLogout() {
        val dialog = AlertDialog.Builder(requireActivity()).create()
        val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogBinding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }

        dialog.setView(dialogBinding.root)
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}
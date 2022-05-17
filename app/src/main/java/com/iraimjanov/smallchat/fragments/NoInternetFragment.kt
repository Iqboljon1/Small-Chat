package com.iraimjanov.smallchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.databinding.FragmentNoInternetBinding

class NoInternetFragment : Fragment() {
    private lateinit var binding: FragmentNoInternetBinding
    private lateinit var networkHelper: NetworkHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNoInternetBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        binding.imageIcRefresh.setOnClickListener {
            if (networkHelper.isNetworkConnected()){
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

}
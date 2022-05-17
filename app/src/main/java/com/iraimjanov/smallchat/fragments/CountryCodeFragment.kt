package com.iraimjanov.smallchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iraimjanov.smallchat.adapters.RVCountryCodeAdapter
import com.iraimjanov.smallchat.cc.CountryDB
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.databinding.FragmentCountryCodeBinding
import com.iraimjanov.smallchat.db.AppDatabase
import com.iraimjanov.smallchat.R

class CountryCodeFragment : Fragment() {
    private lateinit var binding: FragmentCountryCodeBinding
    private lateinit var listCountry: List<CountryDB>
    private lateinit var appDatabase: AppDatabase
    private lateinit var networkHelper: NetworkHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCountryCodeBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_countryCodeFragment_to_noInternetFragment)
        }

        return binding.root
    }

    private fun connection() {
        appDatabase = AppDatabase.getInstance(requireActivity())

        buildRV()
        buildSearchView()
        binding.imageIcBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun buildSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val list = appDatabase.myDao().searchCountryCode(newText.toString())
                binding.rvCountryCodes.adapter = RVCountryCodeAdapter(list, findNavController())
                return true
            }
        })
    }

    private fun buildRV() {
        listCountry = appDatabase.myDao().getAllCountryCode()
        binding.rvCountryCodes.adapter = RVCountryCodeAdapter(listCountry, findNavController())
    }
}
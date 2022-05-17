package com.iraimjanov.smallchat.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.smallchat.adapters.RVSearchGroupsAdapter
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.FragmentSearchBinding
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.R

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var realReference: DatabaseReference
    private lateinit var myGroups: ArrayList<Groups>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_searchFragment_to_noInternetFragment)
        }

        return binding.root
    }

    private fun connection() {
        loadData()
        settingsSearchView()
        binding.imageIcBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadData() {
        realReference = Firebase.database.getReference("groups")
        myGroups = profile.groups
    }

    private fun settingsSearchView() {
        binding.searchView.requestFocus()
        binding.searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(view.findFocus(), 0)
            }
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.toString().trim().isNotEmpty()) {
                    realReference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            realReference.orderByChild("name").startAt(newText)
                                .endAt("$newText\uf8ff")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val listGroups = ArrayList<Groups>()
                                        val hashSet = HashSet<String>()
                                        for (myGroup in myGroups) {
                                            hashSet.add(myGroup.uid)
                                        }
                                        for (child in snapshot.children) {
                                            val groups = child.getValue(Groups::class.java)
                                            if (groups != null) {
                                                if (hashSet.add(groups.uid)) {
                                                    listGroups.add(groups)
                                                }
                                            }
                                        }
                                        binding.rvGroups.adapter = RVSearchGroupsAdapter(listGroups,
                                            object : RVSearchGroupsAdapter.RVClickSearchGroups {
                                                override fun click(groups: Groups) {
                                                    UpdatePersonalUsers().addPersonalUserToGroup(
                                                        groups,
                                                        profile)
                                                    UpdatePersonalUsers().addGroupToUser(groups,
                                                        profile)
                                                    findNavController().popBackStack()
                                                }
                                            })
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                } else {
                    binding.rvGroups.adapter = RVSearchGroupsAdapter(ArrayList(),
                        object : RVSearchGroupsAdapter.RVClickSearchGroups {
                            override fun click(groups: Groups) {}
                        })
                }
                return true
            }
        })
    }
}
package com.iraimjanov.smallchat.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.smallchat.adapters.RVGroupUsersAdapter
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.databinding.FragmentShowGroupInfoBinding
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.R

class ShowGroupInfoFragment : Fragment() {
    private lateinit var binding: FragmentShowGroupInfoBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var realReference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentShowGroupInfoBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_showGroupInfoFragment_to_noInternetFragment)
        }

        return binding.root
    }

    private fun connection() {
        realReference = Firebase.database.getReference("groups").child(PublicData.groups.uid)
        getGroup()
        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun getGroup() {
        realReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Groups::class.java)
                if (group != null) {
                    showActivity(group)
                } else {
                    if (isVisible) {
                        findNavController().popBackStack()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showActivity(group: Groups) {
        binding.tvName.isSelected = true
        binding.tvName.text = group.name
        if (group.imageID.isNotEmpty()) {
            Glide.with(binding.root).load(group.imageUrl).into(binding.imageGroup)
        }
        if (group.listPersonalUsers.size != 1) {
            binding.tvMembersCount.text = "${group.listPersonalUsers.size} members"
            binding.tvMembers.text = "Members"
        } else {
            binding.tvMembersCount.text = "${group.listPersonalUsers.size} member"
            binding.tvMembers.text = "Member"
        }
        binding.rvUsers.adapter = RVGroupUsersAdapter(group.listPersonalUsers, group.admin)
    }
}
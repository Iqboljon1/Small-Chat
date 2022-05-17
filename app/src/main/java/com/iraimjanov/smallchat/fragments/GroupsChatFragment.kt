package com.iraimjanov.smallchat.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.iraimjanov.smallchat.adapters.RVGroupsAdapter
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.DialogDeletePersonalUserBinding
import com.iraimjanov.smallchat.databinding.FragmentGroupsChatBinding
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.R


class GroupsChatFragment : Fragment() {
    private lateinit var binding: FragmentGroupsChatBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var realReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var realReferenceGroups: DatabaseReference
    private lateinit var realReferenceGroupsChats: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentGroupsChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()

        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            navController.navigate(R.id.action_homeFragment_to_noInternetFragment)
        }
    }

    private fun loadData() {
        networkHelper = NetworkHelper(requireActivity())
        navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerParent) as NavHostFragment
        navController = navHostFragment.navController
        auth = Firebase.auth
        realReference = Firebase.database.getReference("users").child(auth.currentUser!!.uid)
        realReferenceGroups = Firebase.database.getReference("groups")
        realReferenceGroupsChats = Firebase.database.getReference("groupsChats")
        storageReference = Firebase.storage.getReference("groupsImage/")
    }

    private fun connection() {
        realReference.child("groups").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isVisible) {
                    val t: GenericTypeIndicator<ArrayList<Groups?>?> =
                        object : GenericTypeIndicator<ArrayList<Groups?>?>() {}
                    val groups: ArrayList<Groups?>? = snapshot.getValue(t)
                    if (groups != null) {
                        val listGroups = ArrayList<Groups>()
                        groups.forEach {
                            listGroups.add(it!!)
                        }
                        buildRV(listGroups)
                    } else {
                        binding.rvGroups.adapter = null
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun buildRV(list: ArrayList<Groups>) {
        binding.rvGroups.adapter =
            RVGroupsAdapter(list, object : RVGroupsAdapter.RVClickGroups {
                override fun click(groups: Groups) {
                    PublicData.groups = groups
                    findNavController().navigate(R.id.action_homeFragment_to_chatGroupFragment)
                }

                override fun delete(groups: Groups) {
                    buildDialogDeleteGroups(groups)
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun buildDialogDeleteGroups(groups: Groups) {
        val dialog = AlertDialog.Builder(requireActivity()).create()
        val dialogBinding = DialogDeletePersonalUserBinding.inflate(layoutInflater)
        dialogBinding.imageProfile.setBackgroundResource(R.drawable.ic_groups)

        if (groups.imageID.isNotEmpty()) {
            Glide.with(dialogBinding.root).load(groups.imageUrl)
                .into(dialogBinding.imageProfile)
        }
        dialogBinding.tvDeleteChat.text = "Delete group"
        dialogBinding.tvDeleteChatDescription.text =
            "Are you sure you want to delete and leave the group ${groups.name}?"
        dialogBinding.tvCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogBinding.tvDelete.setOnClickListener {
            UpdatePersonalUsers().deleteGroupInUsers(groups.uid)
            realReferenceGroups.child(groups.uid).removeValue()
            storageReference.child(groups.uid).delete()
            realReferenceGroupsChats.child(groups.uid).removeValue()
            dialog.cancel()
        }

        dialog.setView(dialogBinding.root)
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}
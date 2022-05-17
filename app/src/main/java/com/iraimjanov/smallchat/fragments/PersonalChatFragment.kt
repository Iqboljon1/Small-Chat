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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.smallchat.adapters.RVUsersAdapter
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.DialogDeletePersonalUserBinding
import com.iraimjanov.smallchat.databinding.FragmentPersonalChatBinding
import com.iraimjanov.smallchat.models.PersonalUsers
import com.iraimjanov.smallchat.R

class PersonalChatFragment : Fragment() {
    private lateinit var binding: FragmentPersonalChatBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var realReference: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPersonalChatBinding.inflate(layoutInflater)
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
        navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerParent) as NavHostFragment
        navController = navHostFragment.navController
        auth = Firebase.auth
        realReference = Firebase.database.getReference("users").child(auth.currentUser!!.uid)
    }

    private fun connection() {
        realReference.child("personalUsers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isVisible) {
                    val t: GenericTypeIndicator<ArrayList<PersonalUsers?>?> =
                        object : GenericTypeIndicator<ArrayList<PersonalUsers?>?>() {}
                    val list: ArrayList<PersonalUsers?>? = snapshot.getValue(t)
                    if (list != null) {
                        val listPersonalUsers = ArrayList<PersonalUsers>()
                        list.forEach {
                            listPersonalUsers.add(it!!)
                        }
                        buildRV(listPersonalUsers)
                    } else {
                        binding.rvUsers.adapter = null
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun buildRV(listPersonalUsers: ArrayList<PersonalUsers>) {
        binding.rvUsers.adapter =
            RVUsersAdapter(listPersonalUsers, object : RVUsersAdapter.RVClickUsers {
                override fun click(personalUsers: PersonalUsers) {
                    PublicData.chatProfile = personalUsers
                    navController.navigate(R.id.action_homeFragment_to_chatFragment)
                }

                override fun delete(personalUsers: PersonalUsers) {
                    buildDialogDeletePersonalUser(personalUsers)
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun buildDialogDeletePersonalUser(personalUsers: PersonalUsers) {
        val dialog = AlertDialog.Builder(requireActivity()).create()
        val dialogBinding = DialogDeletePersonalUserBinding.inflate(layoutInflater)

        if (personalUsers.imageId.isNotEmpty()) {
            Glide.with(dialogBinding.root).load(personalUsers.imageUrl)
                .into(dialogBinding.imageProfile)
        }
        dialogBinding.tvDeleteChatDescription.text =
            "Are you sure you want to delete the chat with ${personalUsers.firstName}?"

        dialogBinding.tvCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogBinding.tvDelete.setOnClickListener {
            UpdatePersonalUsers().deletePersonaUsers(PublicData.profile.uid, personalUsers.uid)
            UpdatePersonalUsers().closeTheConversationBetweenUsers(PublicData.profile.uid,
                personalUsers.uid)
            dialog.cancel()
        }

        dialog.setView(dialogBinding.root)
        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}
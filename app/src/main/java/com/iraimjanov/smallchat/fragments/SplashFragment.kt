package com.iraimjanov.smallchat.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.models.Users
import com.iraimjanov.smallchat.R
import com.iraimjanov.smallchat.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var networkHelper: NetworkHelper
    private var countDownTimer = object : CountDownTimer(2000, 100) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            navigateNextFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())
        if (networkHelper.isNetworkConnected()) {
            auth = FirebaseAuth.getInstance()
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseReference = firebaseDatabase.getReference("users")
            countDownTimer.start()
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_noInternetFragment)
        }
        return binding.root
    }

    private fun navigateNextFragment() {
        if (auth.currentUser != null) {
            firebaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isVisible){
                        if (checkUserAddedFirebaseDatabase(snapshot)) {
                            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                        } else {
                            findNavController().navigate(R.id.action_splashFragment_to_createProfileFragment)
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_welcomeFragment)
        }
    }

    private fun checkUserAddedFirebaseDatabase(snapshot: DataSnapshot): Boolean {
        var boolean = false
        for (child in snapshot.children) {
            val users = child.getValue(Users::class.java)
            if (users != null) {
                if (users.uid == auth.uid) {
                    boolean = true
                    break
                } else {
                    boolean = false
                }
            }
        }
        return boolean
    }
}
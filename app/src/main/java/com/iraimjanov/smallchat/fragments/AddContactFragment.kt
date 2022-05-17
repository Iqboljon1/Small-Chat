package com.iraimjanov.smallchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.iraimjanov.smallchat.R
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData.countryDB
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.FragmentAddContactBinding
import com.iraimjanov.smallchat.db.AppDatabase
import com.iraimjanov.smallchat.models.PersonalUsers
import com.iraimjanov.smallchat.models.Users

class AddContactFragment : Fragment() {
    private lateinit var binding: FragmentAddContactBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var appDatabase: AppDatabase
    private lateinit var realDatabase: FirebaseDatabase
    private lateinit var realReference: DatabaseReference
    private lateinit var number: String
    private var numberFound = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddContactBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_addContactFragment_to_noInternetFragment)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.edtCountryName.setText(countryDB.names)
        binding.edtCode.setText(countryDB.codes)
        binding.edtNumber.requestFocus()
    }

    private fun connection() {
        appDatabase = AppDatabase.getInstance(requireActivity())
        realDatabase = FirebaseDatabase.getInstance()
        realReference = realDatabase.getReference("users")

        binding.nextCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_addContactFragment_to_countryCodeFragment)
        }

        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.edtCode.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                val list = appDatabase.myDao().searchOneCountryCode(it.toString())
                if (list.isNotEmpty()) {
                    binding.edtCountryName.setText(list[0].names)
                } else {
                    binding.edtCountryName.setText(R.string.invalid)
                }
            } else {
                binding.edtCountryName.setText(R.string.choose)
            }
        }

        binding.imageDone.setOnClickListener {
            val country = binding.edtCountryName.text.toString().trim()
            val number = binding.edtNumber.text.toString().trim()
            val code = binding.edtCode.text.toString().trim()
            if (country != "Choose the country" && country != "Invalid country code" && number.isNotEmpty()) {
                this.number = code + number
                saveContact(code + number)
            } else {
                Toast.makeText(requireActivity(),
                    "The country code does not include an error or number",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveContact(number: String) {
        if (number != profile.number) {
            realReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    searchContact(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            Toast.makeText(requireActivity(), "This is yours your phone number", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun searchContact(snapshot: DataSnapshot) {
        if (notAddedBeforeContact()) {
            for (child in snapshot.children) {
                val users = child.getValue(Users::class.java)
                if (users != null) {
                    checkContact(users)
                }
            }
            if (!numberFound) {
                Toast.makeText(requireActivity(),
                    "This number is not registered",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(requireActivity(),
                "This number is already available on your contact",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun notAddedBeforeContact(): Boolean {
        var boolean = true
        for (users in profile.personalUsers) {
            if (users.number == this.number) {
                boolean = false
            }
        }
        return boolean
    }

    private fun checkContact(users: Users) {
        if (users.number == this.number) {
            numberFound = true
            val personalUsers = PersonalUsers(users.uid,
                users.number,
                users.firstName,
                users.lastName,
                users.imageId,
                users.imageUrl)
            profile.personalUsers.add(personalUsers)
            realReference.child(profile.uid).setValue(profile)
            UpdatePersonalUsers().addPersonalUser(profile, users)
            findNavController().popBackStack()
        }
    }
}
package com.iraimjanov.smallchat.fragments

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.databinding.FragmentCreateProfileBinding
import com.iraimjanov.smallchat.models.Users
import com.iraimjanov.smallchat.R
import java.io.ByteArrayOutputStream

class CreateProfileFragment : Fragment() {
    private lateinit var binding: FragmentCreateProfileBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseReference: DatabaseReference
    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateProfileBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())

        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_homeFragment_to_noInternetFragment)
        }

        return binding.root
    }

    private fun connection() {
        loadData()

        binding.swipeRefreshLayout.isEnabled = false
        binding.cardImageAdd.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                getImageContent.launch("image/*")
            }
        }

        binding.imageDone.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                if (binding.edtFirstname.text.toString().trim().isNotEmpty()) {
                    save()
                } else {
                    Toast.makeText(requireActivity(),
                        "You must enter your name",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.imageNoImage.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                uri = null
                Glide.with(requireActivity()).load(R.drawable.ic_user).centerCrop()
                    .into(binding.profileImage)
            }
        }

        binding.imageBack.setOnClickListener {
            requireActivity().finish()
        }

    }

    private fun loadData() {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        storageReference = storage.getReference("profileImage/")
        firebaseReference = firebaseDatabase.getReference("users")
    }

    private fun save() {
        binding.swipeRefreshLayout.isRefreshing = true
        if (uri != null) {
            saveImageToFirebase()
        } else {
            saveDateToFirebaseDatabase("")
        }
    }

    private fun saveImageToFirebase() {
        val bitmap: Bitmap = binding.profileImage.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        storageReference.child(auth.currentUser!!.uid).putBytes(byteArray)
            .addOnSuccessListener { it ->
                it.storage.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "addOnSuccessListener")
                    saveDateToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener {
            Log.d(TAG, "addOnFailureListener", it)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun saveDateToFirebaseDatabase(imageUri: String) {
        val firstname = binding.edtFirstname.text.toString().trim()
        val lastname = binding.edtLastname.text.toString().trim()
        val users = if (imageUri.isNotEmpty()) {
            Users(auth.currentUser!!.uid,
                auth.currentUser!!.phoneNumber!!,
                firstname,
                lastname,
                auth.currentUser!!.uid, imageUri , ArrayList() , ArrayList())
        } else {
            Users(auth.currentUser!!.uid,
                auth.currentUser!!.phoneNumber!!,
                firstname,
                lastname,
                "",
                imageUri , ArrayList() , ArrayList())
        }
        firebaseReference.child(auth.currentUser!!.uid).setValue(users).addOnSuccessListener {
            Log.d(TAG, "addOnSuccessListener")
            binding.swipeRefreshLayout.isRefreshing = false
            findNavController().navigate(R.id.action_createProfileFragment_to_homeFragment)
        }.addOnFailureListener {
            Log.d(TAG, "addOnFailureListener", it)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        Glide.with(requireActivity()).load(it).centerCrop().into(binding.profileImage)
        val layoutParams: ViewGroup.LayoutParams = binding.profileImage.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.profileImage.layoutParams = layoutParams
        uri = it
    }

}
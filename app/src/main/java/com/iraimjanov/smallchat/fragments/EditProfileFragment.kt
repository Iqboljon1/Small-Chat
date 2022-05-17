package com.iraimjanov.smallchat.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.FragmentEditProfileBinding
import com.iraimjanov.smallchat.models.Users
import com.iraimjanov.smallchat.R
import java.io.ByteArrayOutputStream

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var storageDatabase: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var realDatabase: FirebaseDatabase
    private lateinit var realReference: DatabaseReference
    private var uri: Uri? = null
    private var deleteImage = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())
        realDatabase = FirebaseDatabase.getInstance()
        realReference = realDatabase.getReference("users")
        storageDatabase = FirebaseStorage.getInstance()
        storageReference = storageDatabase.getReference("profileImage/")

        if (networkHelper.isNetworkConnected()) {
            updatePersonalUsersAndGroups()
        } else {
            findNavController().navigate(R.id.action_editProfileFragment_to_noInternetFragment)
        }

        return binding.root
    }

    private fun connection() {
        showActivity()

        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardImageAdd.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                getImageContent.launch("image/*")
            }
        }

        binding.imageNoImage.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                deleteImage = true
                uri = null
                Glide.with(requireActivity()).load(R.drawable.ic_user).centerCrop()
                    .into(binding.profileImage)
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

    }

    private fun showActivity() {
        binding.swipeRefreshLayout.isEnabled = false
        binding.edtFirstname.setText(PublicData.profile.firstName)
        binding.edtLastname.setText(PublicData.profile.lastName)
        if (PublicData.profile.imageId.isNotEmpty()) {
            Glide.with(binding.root).load(PublicData.profile.imageUrl).into(binding.profileImage)
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

    private fun save() {
        if (uri != null) {
            saveImageToFirebase()
        } else {
            saveDateToFirebaseDatabase("")
        }
    }

    private fun saveImageToFirebase() {
        binding.swipeRefreshLayout.isRefreshing = true
        val bitmap: Bitmap = binding.profileImage.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        storageReference.child(PublicData.profile.uid).putBytes(byteArray)
            .addOnSuccessListener { it ->
                it.storage.downloadUrl.addOnSuccessListener {
                    Log.d(ContentValues.TAG, "addOnSuccessListener")
                    saveDateToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener {
                Log.d(ContentValues.TAG, "addOnFailureListener", it)
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun saveDateToFirebaseDatabase(imageUri: String) {
        val firstname = binding.edtFirstname.text.toString().trim()
        val lastname = binding.edtLastname.text.toString().trim()
        val users = if (imageUri.isNotEmpty()) {
            Users(PublicData.profile.uid,
                PublicData.profile.number,
                firstname,
                lastname,
                PublicData.profile.uid,
                imageUri, PublicData.profile.personalUsers, PublicData.profile.groups)
        } else {
            if (deleteImage) {
                Users(PublicData.profile.uid,
                    PublicData.profile.number,
                    firstname,
                    lastname,
                    "",
                    imageUri, PublicData.profile.personalUsers, PublicData.profile.groups)
            } else {
                Users(PublicData.profile.uid,
                    PublicData.profile.number,
                    firstname,
                    lastname,
                    PublicData.profile.imageId,
                    PublicData.profile.imageUrl,
                    PublicData.profile.personalUsers,
                    PublicData.profile.groups)
            }
        }
        realReference.child(PublicData.profile.uid).setValue(users).addOnSuccessListener {
            Log.d(ContentValues.TAG, "addOnSuccessListener")
            UpdatePersonalUsers().updatePersonalUsers(users)
            UpdatePersonalUsers().updatePersonalUsersInGroup(users)
            UpdatePersonalUsers().updateChatsInGroup(users)
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireActivity(), "Edited", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }.addOnFailureListener {
            Toast.makeText(requireActivity(), "Editing error", Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun updatePersonalUsersAndGroups() {
        realReference.child(PublicData.profile.uid).get().addOnSuccessListener {
            val user = it.getValue(Users::class.java)
            if (user != null) {
                PublicData.profile.personalUsers = user.personalUsers
                PublicData.profile.groups = user.groups
                connection()
            }
        }
    }

}
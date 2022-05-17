package com.iraimjanov.smallchat.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData.profile
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.FragmentAddGroupBinding
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.models.PersonalUsers
import com.iraimjanov.smallchat.R
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class AddGroupFragment : Fragment() {
    private lateinit var binding: FragmentAddGroupBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var realReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var time = getTime() + profile.uid
    private var uri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddGroupBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())
        if (networkHelper.isNetworkConnected()) {
            connection()
        } else {
            findNavController().navigate(R.id.action_addGroupFragment_to_noInternetFragment)
        }


        return binding.root
    }

    private fun connection() {
        binding.swipeRefreshLayout.isEnabled = false
        realReference = Firebase.database.getReference("groups")
        storageReference = Firebase.storage.getReference("groupsImage/")

        binding.cardImageAdd.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                getImageContent.launch("image/*")
            }
        }

        realReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hashSet = HashSet<String>()
                for (child in snapshot.children) {
                    val group = child.getValue(Groups::class.java)
                    if (group != null) {
                        hashSet.add(group.name)
                    }
                }
                binding.imageDone.setOnClickListener {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        if (binding.edtGroupName.text.toString().trim().isNotEmpty()) {
                            if (hashSet.add(binding.edtGroupName.text.toString().trim())){
                                save()
                            }else{
                                Toast.makeText(requireActivity(),
                                    "Such a group already exists",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireActivity(),
                                "You must enter a group name",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.imageNoImage.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                uri = null
                Glide.with(requireActivity()).load(R.drawable.ic_groups).centerCrop()
                    .into(binding.groupImage)
            }
        }

        binding.imageBack.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                findNavController().popBackStack()
            }
        }
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
        val bitmap: Bitmap = binding.groupImage.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        storageReference.child(time).putBytes(byteArray)
            .addOnSuccessListener { it ->
                it.storage.downloadUrl.addOnSuccessListener {
                    saveDateToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener {
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun saveDateToFirebaseDatabase(imageUri: String) {
        val groupName = binding.edtGroupName.text.toString().trim()
        val groups = if (imageUri.isNotEmpty()) {
            Groups(time, groupName, time, imageUri, profile.uid, ArrayList())
        } else {
            Groups(time, groupName, "", imageUri, profile.uid, ArrayList())
        }
        groups.listPersonalUsers.add(PersonalUsers(profile.uid,
            profile.number,
            profile.firstName,
            profile.lastName,
            profile.imageId,
            profile.imageUrl))
        realReference.child(groups.uid).setValue(groups).addOnSuccessListener {
            profile.groups.add(groups)
            UpdatePersonalUsers().addGroupToUser(groups, profile)
            binding.swipeRefreshLayout.isRefreshing = false
            findNavController().popBackStack()
        }.addOnFailureListener {
            Toast.makeText(requireActivity(), "Network or Server Error", Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        Glide.with(requireActivity()).load(it).centerCrop().into(binding.groupImage)
        val layoutParams: ViewGroup.LayoutParams = binding.groupImage.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.groupImage.layoutParams = layoutParams
        uri = it
    }


    private fun getTime(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

}
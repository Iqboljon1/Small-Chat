package com.iraimjanov.smallchat.fragments

import android.content.ContentValues
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.iraimjanov.smallchat.data.NetworkHelper
import com.iraimjanov.smallchat.data.PublicData.groups
import com.iraimjanov.smallchat.data.UpdatePersonalUsers
import com.iraimjanov.smallchat.databinding.FragmentEditGroupBinding
import com.iraimjanov.smallchat.models.Groups
import com.iraimjanov.smallchat.R
import java.io.ByteArrayOutputStream

class EditGroupFragment : Fragment() {
    private lateinit var binding: FragmentEditGroupBinding
    private lateinit var networkHelper: NetworkHelper
    private lateinit var referenceGroups: DatabaseReference
    private lateinit var referenceStorage: StorageReference
    private lateinit var group: Groups
    private var uri: Uri? = null
    private var deleteImage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditGroupBinding.inflate(layoutInflater)
        networkHelper = NetworkHelper(requireActivity())
        referenceGroups = Firebase.database.getReference("groups")
        referenceStorage = Firebase.storage.getReference("groupsImage/")

        if (networkHelper.isNetworkConnected()) {
            referenceGroups.child(groups.uid).get().addOnSuccessListener {
                group = it.getValue(Groups::class.java)!!
                connection()
            }
        } else {
            findNavController().navigate(R.id.action_editGroupFragment_to_noInternetFragment)
        }
        return binding.root
    }

    private fun connection() {
        showActivity()

        binding.imageBack.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                findNavController().popBackStack()
            }
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
                binding.imageGroup.setImageResource(0)
            }
        }

        referenceGroups.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hashSet = HashSet<String>()
                for (child in snapshot.children) {
                    val groups = child.getValue(Groups::class.java)
                    if (groups != null && groups.name != group.name) {
                        hashSet.add(groups.name)
                    }
                }
                binding.imageDone.setOnClickListener {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        if (binding.edtName.text.toString().trim().isNotEmpty()) {
                            if (hashSet.add(binding.edtName.text.toString().trim())) {
                                save()
                            } else {
                                Toast.makeText(requireActivity(),
                                    "Such a group already exists",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireActivity(),
                                "You must enter group name",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun showActivity() {
        binding.swipeRefreshLayout.isEnabled = false
        binding.edtName.setText(group.name)
        if (group.imageID.isNotEmpty()) {
            Glide.with(binding.root).load(group.imageUrl).into(binding.imageGroup)
        }
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
        val bitmap: Bitmap = binding.imageGroup.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        referenceStorage.child(groups.uid).putBytes(byteArray)
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

    private fun saveDateToFirebaseDatabase(url: String) {
        val name = binding.edtName.text.toString().trim()
        val newGroup = if (url.isNotEmpty()) {
            Groups(group.uid,
                name,
                group.uid,
                url,
                group.admin,
                group.listPersonalUsers)
        } else {
            if (deleteImage) {
                Groups(group.uid,
                    name,
                    "",
                    url,
                    group.admin,
                    group.listPersonalUsers)
            } else {
                Groups(group.uid,
                    name,
                    group.uid,
                    group.imageUrl,
                    group.admin,
                    group.listPersonalUsers)
            }
        }
        referenceGroups.child(group.uid).setValue(newGroup).addOnSuccessListener {
            UpdatePersonalUsers().updateGroupsInUsers(newGroup)
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireActivity(), "Edited", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }.addOnFailureListener {
            Toast.makeText(requireActivity(), "Editing error", Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        Glide.with(requireActivity()).load(it).centerCrop().into(binding.imageGroup)
        val layoutParams: ViewGroup.LayoutParams = binding.imageGroup.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.imageGroup.layoutParams = layoutParams
        uri = it
    }

}